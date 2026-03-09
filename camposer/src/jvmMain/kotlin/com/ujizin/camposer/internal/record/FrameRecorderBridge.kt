package com.ujizin.camposer.internal.record

import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Frame
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

internal fun ffmpegRecorderBridge(
  filename: String,
  width: Int,
  height: Int,
  audioChannels: Int,
): FrameRecorderBridge =
  FFmpegFrameRecorderBridge(FFmpegFrameRecorder(filename, width, height, audioChannels))

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
