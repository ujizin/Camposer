package com.ujizin.camposer.internal.record

import com.ujizin.camposer.internal.capture.JvmCameraCapture
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FPS
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_HEIGHT
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_WIDTH
import org.bytedeco.opencv.opencv_core.Mat
import java.nio.ShortBuffer

internal interface FrameRecorderBridge {
  fun start()

  fun record(frame: Frame)

  fun recordSamples(
    sampleRate: Int,
    audioChannels: Int,
    samples: ShortBuffer,
  ): Boolean

  fun stop()

  fun release()

  var videoCodec: Int
  var audioCodec: Int
  var frameRate: Double
  var sampleRate: Int
  var audioChannels: Int
  var videoBitrate: Int
  var audioBitrate: Int
}

internal interface MatFrameConverter {
  fun convert(mat: Mat): Frame?

  fun close()
}

private class FFmpegFrameRecorderBridge(
  private val delegate: FFmpegFrameRecorder,
) : FrameRecorderBridge {
  override fun start() = delegate.start()

  override fun record(frame: Frame) = delegate.record(frame)

  override fun recordSamples(
    sampleRate: Int,
    audioChannels: Int,
    samples: ShortBuffer,
  ): Boolean = delegate.recordSamples(sampleRate, audioChannels, samples)

  override fun stop() = delegate.stop()

  override fun release() = delegate.release()

  override var videoCodec: Int
    get() = delegate.videoCodec
    set(value) {
      delegate.videoCodec = value
    }

  override var audioCodec: Int
    get() = delegate.audioCodec
    set(value) {
      delegate.audioCodec = value
    }

  override var frameRate: Double
    get() = delegate.frameRate
    set(value) {
      delegate.frameRate = value
    }

  override var sampleRate: Int
    get() = delegate.sampleRate
    set(value) {
      delegate.sampleRate = value
    }

  override var audioChannels: Int
    get() = delegate.audioChannels
    set(value) {
      delegate.audioChannels = value
    }

  override var videoBitrate: Int
    get() = delegate.videoBitrate
    set(value) {
      delegate.videoBitrate = value
    }

  override var audioBitrate: Int
    get() = delegate.audioBitrate
    set(value) {
      delegate.audioBitrate = value
    }
}

private class OpenCVMatFrameConverter : MatFrameConverter {
  private val delegate = OpenCVFrameConverter.ToMat()

  override fun convert(mat: Mat): Frame? = delegate.convert(mat)

  override fun close() = delegate.close()
}

internal class JvmVideoRecorder(
  private val filename: String,
  private val capture: JvmCameraCapture,
  private val sampleRate: Int = 44100,
  private val audioChannels: Int = 1,
  private val recorderFactory: (String, Int, Int, Int) -> FrameRecorderBridge = { fn, w, h, ch ->
    FFmpegFrameRecorderBridge(FFmpegFrameRecorder(fn, w, h, ch))
  },
  private val converterFactory: () -> MatFrameConverter = { OpenCVMatFrameConverter() },
) {
  private var recorder: FrameRecorderBridge? = null
  private val converter: MatFrameConverter = converterFactory()

  // Throws if the underlying recorder's start() fails.
  internal fun start() {
    val width = capture.get(CAP_PROP_FRAME_WIDTH).toInt()
    val height = capture.get(CAP_PROP_FRAME_HEIGHT).toInt()
    val fps = capture.get(CAP_PROP_FPS).let { if (it <= 0) 30.0 else it }

    val rec = recorderFactory(filename, width, height, audioChannels)
    rec.videoCodec = avcodec.AV_CODEC_ID_H264
    rec.audioCodec = avcodec.AV_CODEC_ID_AAC
    rec.frameRate = fps
    rec.sampleRate = sampleRate
    rec.audioChannels = audioChannels
    rec.videoBitrate = 0
    rec.audioBitrate = 128_000
    rec.start()
    recorder = rec
  }

  internal fun record(mat: Mat) {
    val rec = recorder ?: return
    val frame = converter.convert(mat) ?: return
    rec.record(frame)
  }

  internal fun recordSamples(samples: ShortBuffer) {
    recorder?.recordSamples(sampleRate, audioChannels, samples)
  }

  // Throws if the underlying recorder's stop() fails.
  internal fun stop() {
    recorder?.stop()
    recorder?.release()
    recorder = null
    converter.close()
  }
}
