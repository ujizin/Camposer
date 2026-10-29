package com.ujizin.camposer.internal.controller

import com.ujizin.camposer.error.RecordNotInitializedException
import com.ujizin.camposer.internal.core.jvm.JvmFrame
import com.ujizin.camposer.internal.error.RecordingInProgressException
import com.ujizin.camposer.internal.record.JvmAudioCapture
import com.ujizin.camposer.internal.record.JvmVideoRecorder
import com.ujizin.camposer.internal.record.RecordingConfig
import com.ujizin.camposer.internal.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.ShortBuffer

private const val NANOS_PER_MICRO = 1_000L

/**
 * Drives one recording at a time: opens the microphone, feeds frames and samples to a
 * [JvmVideoRecorder], and keeps audio and video in sync across pauses.
 *
 * Frames arrive on the capture thread and samples on the audio thread. A single lock
 * serializes every recorder call.
 */
internal class JvmRecordController(
  private val recorderFactory: JvmVideoRecorder.Factory,
  private val audioCaptureFactory: JvmAudioCapture.Factory,
  private val clock: () -> Long = System::nanoTime,
) {
  private class ActiveRecording(
    val filename: String,
    val recorder: JvmVideoRecorder,
    val audioCapture: JvmAudioCapture?,
    val isMirrored: Boolean,
    val onVideoCaptured: (Result<String>) -> Unit,
    val startedAtNanos: Long,
  ) {
    var pausedNanos: Long = 0L
    var pauseStartedAtNanos: Long? = null

    val isPaused: Boolean
      get() = pauseStartedAtNanos != null
  }

  private val lock = Any()
  private var activeRecording: ActiveRecording? = null

  private val _isRecording = MutableStateFlow(false)
  val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

  private val _isMuted = MutableStateFlow(false)
  val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

  @Suppress("LongParameterList")
  fun start(
    filename: String,
    width: Int,
    height: Int,
    frameRate: Double,
    isMirrored: Boolean,
    onVideoCaptured: (Result<String>) -> Unit,
  ) {
    synchronized(lock) {
      if (activeRecording != null) {
        onVideoCaptured(Result.failure(RecordingInProgressException()))
        return
      }
    }

    val audioCapture = openAudioCapture()
    val config = RecordingConfig(
      filename = filename,
      width = width,
      height = height,
      frameRate = frameRate,
      audioChannels = audioCapture?.channels ?: 0,
      sampleRate = audioCapture?.sampleRate ?: 0,
    )

    val recorder = runCatching { recorderFactory.create(config).also { it.start() } }
      .getOrElse { error ->
        audioCapture?.stop()
        onVideoCaptured(Result.failure(error))
        return
      }

    synchronized(lock) {
      activeRecording = ActiveRecording(
        filename = filename,
        recorder = recorder,
        audioCapture = audioCapture,
        isMirrored = isMirrored,
        onVideoCaptured = onVideoCaptured,
        startedAtNanos = clock(),
      )
    }
    _isRecording.update { true }
  }

  /** Called from the capture thread for every frame. */
  fun onFrame(frame: JvmFrame) {
    synchronized(lock) {
      val recording = activeRecording ?: return
      if (recording.isPaused) return

      val elapsedNanos = clock() - recording.startedAtNanos - recording.pausedNanos
      runCatching {
        recording.recorder.recordFrame(
          frame = frame,
          isMirrored = recording.isMirrored,
          timestampMicros = elapsedNanos / NANOS_PER_MICRO,
        )
      }.onFailure { error -> failRecordingLocked(error) }
    }
  }

  fun pause(): Result<Boolean> =
    synchronized(lock) {
      val recording = activeRecording ?: return Result.failure(RecordNotInitializedException())
      if (!recording.isPaused) {
        recording.pauseStartedAtNanos = clock()
      }
      Result.success(true)
    }

  fun resume(): Result<Boolean> =
    synchronized(lock) {
      val recording = activeRecording ?: return Result.failure(RecordNotInitializedException())
      recording.pauseStartedAtNanos?.let { pausedAt ->
        recording.pausedNanos += clock() - pausedAt
        recording.pauseStartedAtNanos = null
      }
      Result.success(true)
    }

  fun stop(): Result<Boolean> {
    val recording = synchronized(lock) {
      activeRecording?.also { activeRecording = null }
    } ?: return Result.failure(RecordNotInitializedException())

    finish(recording)
    val result = runCatching { recording.recorder.stop() }
    recording.onVideoCaptured(result.map { recording.filename })
    return result.map { true }
  }

  fun mute(isMuted: Boolean): Result<Boolean> =
    synchronized(lock) {
      if (activeRecording == null) return Result.failure(RecordNotInitializedException())
      _isMuted.update { isMuted }
      Result.success(true)
    }

  fun dispose() {
    if (_isRecording.value) stop()
  }

  private fun openAudioCapture(): JvmAudioCapture? =
    runCatching {
      audioCaptureFactory.create().also { audioCapture ->
        audioCapture.start(::onSamples)
      }
    }.onFailure { error ->
      Logger.error("Microphone unavailable, recording video without audio", error)
    }.getOrNull()

  private fun onSamples(samples: ShortBuffer) {
    synchronized(lock) {
      val recording = activeRecording ?: return
      if (recording.isPaused) return

      val payload = if (_isMuted.value) silenceOf(samples) else samples
      runCatching { recording.recorder.recordSamples(payload) }
        .onFailure { error -> failRecordingLocked(error) }
    }
  }

  private fun silenceOf(samples: ShortBuffer): ShortBuffer =
    ShortBuffer.allocate(samples.remaining())

  /** Must be called while holding [lock]. */
  private fun failRecordingLocked(error: Throwable) {
    val recording = activeRecording ?: return
    activeRecording = null
    finish(recording)
    runCatching { recording.recorder.stop() }
      .onFailure { stopError -> error.addSuppressed(stopError) }
    recording.onVideoCaptured(Result.failure(error))
  }

  private fun finish(recording: ActiveRecording) {
    recording.audioCapture?.stop()
    _isRecording.update { false }
    _isMuted.update { false }
  }
}
