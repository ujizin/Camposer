package com.ujizin.camposer.internal.record

import com.ujizin.camposer.internal.capture.JvmCameraCapture
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FPS
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_HEIGHT
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_WIDTH
import org.bytedeco.opencv.opencv_core.Mat
import java.nio.ShortBuffer

internal class JvmVideoRecorder(
  private val filename: String,
  private val capture: JvmCameraCapture,
  private val sampleRate: Int = 44100,
  private val audioChannels: Int = 1,
  private val recorderFactory: (
    String,
    Int,
    Int,
    Int,
  ) -> FrameRecorderBridge = ::ffmpegRecorderBridge,
  private val converterFactory: () -> MatFrameConverter = ::openCVMatFrameConverter,
) {
  private val recorderLock = Any()
  private var recorder: FrameRecorderBridge? = null
  private var converter: MatFrameConverter? = converterFactory()

  // Throws if the underlying recorder's start() fails.
  internal fun start() {
    synchronized(recorderLock) {
      val width = capture.get(CAP_PROP_FRAME_WIDTH).toInt()
      val height = capture.get(CAP_PROP_FRAME_HEIGHT).toInt()
      val fps = capture.get(CAP_PROP_FPS).let { if (it <= 0) 30.0 else it }

      val rec = recorderFactory(filename, width, height, audioChannels)
      try {
        rec.videoCodec = avcodec.AV_CODEC_ID_H264
        rec.audioCodec = avcodec.AV_CODEC_ID_AAC
        rec.frameRate = fps
        rec.sampleRate = sampleRate
        rec.audioChannels = audioChannels
        rec.videoBitrate = 0
        rec.audioBitrate = 128_000
        rec.start()
        recorder = rec
      } catch (e: Exception) {
        closeOnFailure(rec, e)
      }
    }
  }

  internal fun record(mat: Mat) {
    synchronized(recorderLock) {
      val rec = recorder ?: return
      val frame = converter?.convert(mat) ?: return
      rec.record(frame)
    }
  }

  internal fun recordSamples(samples: ShortBuffer) {
    synchronized(recorderLock) {
      recorder?.recordSamples(sampleRate, audioChannels, samples)
    }
  }

  // Throws if the underlying recorder's stop() fails.
  internal fun stop() {
    synchronized(recorderLock) {
      val rec = recorder ?: return
      var failure: Exception? = null

      try {
        rec.stop()
      } catch (e: Exception) {
        failure = e
      }

      try {
        rec.release()
      } catch (e: Exception) {
        failure = failure.withSuppressed(e)
      } finally {
        recorder = null
      }

      try {
        closeConverter()
      } catch (e: Exception) {
        failure = failure.withSuppressed(e)
      }

      failure?.let { throw it }
    }
  }

  private fun closeOnFailure(
    recorder: FrameRecorderBridge,
    failure: Exception,
  ): Nothing {
    var finalFailure: Exception = failure
    try {
      recorder.release()
    } catch (e: Exception) {
      finalFailure = finalFailure.withSuppressed(e)
    } finally {
      this.recorder = null
    }

    try {
      closeConverter()
    } catch (e: Exception) {
      finalFailure = finalFailure.withSuppressed(e)
    }

    throw finalFailure
  }

  private fun closeConverter() {
    val currentConverter = converter ?: return
    converter = null
    currentConverter.close()
  }

  private fun Exception?.withSuppressed(suppressed: Exception): Exception =
    when (this) {
      null -> suppressed
      else -> apply { addSuppressed(suppressed) }
    }

  internal fun interface Factory {
    fun create(
      filename: String,
      capture: JvmCameraCapture,
    ): JvmVideoRecorder
  }

  internal companion object : Factory {
    override fun create(
      filename: String,
      capture: JvmCameraCapture,
    ): JvmVideoRecorder = JvmVideoRecorder(filename, capture)
  }
}
