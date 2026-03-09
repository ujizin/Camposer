package com.ujizin.camposer.controller.record

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.internal.core.CameraEngine
import com.ujizin.camposer.internal.core.JvmCameraEngine
import com.ujizin.camposer.internal.record.JvmAudioCapture
import com.ujizin.camposer.internal.record.JvmVideoRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.bytedeco.opencv.opencv_core.Mat

internal actual class DefaultRecordController(
  private val cameraEngine: CameraEngine,
  private val videoRecorderFactory: JvmVideoRecorder.Factory = JvmVideoRecorder,
  private val audioFactory: () -> JvmAudioCapture = { JvmAudioCapture() },
) : RecordController {
  private data class ActiveRecording(
    val filename: String,
    val callback: (CaptureResult<String>) -> Unit,
    val videoRecorder: JvmVideoRecorder?,
    val audioCapture: JvmAudioCapture?,
  )

  private val stateLock = Any()
  private val _isMuted = MutableStateFlow(false)
  actual override val isMuted: StateFlow<Boolean> = _isMuted

  private val _isRecording = MutableStateFlow(false)
  actual override val isRecording: StateFlow<Boolean> = _isRecording

  private var videoRecorder: JvmVideoRecorder? = null
  private var audioCapture: JvmAudioCapture? = null
  private var pendingCallback: ((CaptureResult<String>) -> Unit)? = null
  private var pendingFilename: String? = null
  private val recordingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  private val engine get() = cameraEngine as JvmCameraEngine

  actual override fun startRecording(
    filename: String,
    onVideoCaptured: (CaptureResult<String>) -> Unit,
  ) {
    synchronized(stateLock) {
      if (_isRecording.value) return
    }

    val video = videoRecorderFactory.create(filename, engine.capture)
    val audio = audioFactory()

    // Audio starts before video intentionally. If audio delivers samples before
    // video.start() completes, JvmVideoRecorder.recordSamples() no-ops via its
    // recorder-null guard — no samples are lost, and no exception is thrown.
    try {
      audio.start(recordingScope) { samples -> video.recordSamples(samples) }
    } catch (e: Exception) {
      onVideoCaptured(CaptureResult.Error(e))
      return
    }

    try {
      video.start()
    } catch (e: Exception) {
      audio.stop()
      onVideoCaptured(CaptureResult.Error(e))
      return
    }

    synchronized(stateLock) {
      videoRecorder = video
      audioCapture = audio
      pendingFilename = filename
      pendingCallback = onVideoCaptured
    }

    engine.capture.addFrameListener(frameListener)
    _isRecording.update { true }
  }

  private val frameListener: (Mat) -> Unit = { mat ->
    val recorder = synchronized(stateLock) { videoRecorder }
    if (recorder != null) {
      try {
        recorder.record(mat)
      } catch (e: Exception) {
        failRecording(e)
      }
    }
  }

  actual override fun stopRecording(): Result<Boolean> {
    val activeRecording =
      takeActiveRecording()
        ?: return Result.failure(IllegalStateException("Not recording"))

    engine.capture.removeFrameListener(frameListener)
    activeRecording.audioCapture?.stop()
    return try {
      activeRecording.videoRecorder?.stop()
      activeRecording.callback(CaptureResult.Success(activeRecording.filename))
      Result.success(true)
    } catch (e: Exception) {
      activeRecording.callback(CaptureResult.Error(e))
      Result.failure(e)
    }
  }

  private fun takeActiveRecording(): ActiveRecording? =
    synchronized(stateLock) {
      val filename = pendingFilename ?: return@synchronized null
      val callback = pendingCallback ?: return@synchronized null
      val activeRecording = ActiveRecording(filename, callback, videoRecorder, audioCapture)
      videoRecorder = null
      audioCapture = null
      pendingFilename = null
      pendingCallback = null
      _isRecording.update { false }
      _isMuted.update { false }
      activeRecording
    }

  private fun failRecording(error: Exception) {
    val activeRecording = takeActiveRecording() ?: return
    engine.capture.removeFrameListener(frameListener)
    activeRecording.audioCapture?.stop()

    var finalError = error
    try {
      activeRecording.videoRecorder?.stop()
    } catch (stopError: Exception) {
      finalError.addSuppressed(stopError)
    }

    activeRecording.callback(CaptureResult.Error(finalError))
  }

  actual override fun pauseRecording(): Result<Boolean> =
    Result.failure(UnsupportedOperationException("Pause is not supported on Desktop"))

  actual override fun resumeRecording(): Result<Boolean> =
    Result.failure(UnsupportedOperationException("Resume is not supported on Desktop"))

  actual override fun muteRecording(isMuted: Boolean): Result<Boolean> {
    _isMuted.update { isMuted }
    synchronized(stateLock) { audioCapture }?.mute(isMuted)
    return Result.success(true)
  }

  internal fun dispose() {
    if (_isRecording.value) {
      stopRecording()
    }
    recordingScope.cancel()
  }
}
