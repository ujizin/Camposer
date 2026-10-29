package com.ujizin.camposer.internal.record

import com.ujizin.camposer.internal.core.jvm.JvmFrame
import org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_AAC
import org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264
import org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_YUV420P
import org.bytedeco.javacv.FFmpegFrameRecorder
import java.nio.ShortBuffer

private const val BITS_PER_PIXEL = 4
private const val AUDIO_BITRATE = 128_000

/**
 * [JvmVideoRecorder] backed by FFmpeg. Encodes H.264 video and AAC audio into the container
 * chosen by the file extension (for example `.mp4` or `.mov`).
 */
internal class FFmpegVideoRecorder(
  private val config: RecordingConfig,
) : JvmVideoRecorder {
  private val recorder =
    FFmpegFrameRecorder(config.filename, config.width, config.height, config.audioChannels)
      .apply {
        videoCodec = AV_CODEC_ID_H264
        pixelFormat = AV_PIX_FMT_YUV420P
        frameRate = config.frameRate
        videoBitrate = config.width * config.height * BITS_PER_PIXEL
        setVideoOption("preset", "ultrafast")
        setVideoOption("tune", "zerolatency")

        if (config.audioChannels > 0) {
          audioCodec = AV_CODEC_ID_AAC
          sampleRate = config.sampleRate
          audioChannels = config.audioChannels
          audioBitrate = AUDIO_BITRATE
        }
      }

  override fun start() {
    recorder.start()
  }

  override fun recordFrame(
    frame: JvmFrame,
    isMirrored: Boolean,
    timestampMicros: Long,
  ) {
    // The recorder maps the timestamp to a frame number at the declared frame rate and
    // advances by one after each frame, so only a later timestamp needs to move it forward.
    if (timestampMicros > recorder.timestamp) {
      recorder.timestamp = timestampMicros
    }
    recorder.record(frame.toRecorderFrame(isMirrored))
  }

  override fun recordSamples(samples: ShortBuffer) {
    recorder.recordSamples(config.sampleRate, config.audioChannels, samples)
  }

  override fun stop() {
    try {
      recorder.stop()
    } finally {
      recorder.release()
    }
  }

  internal companion object Factory : JvmVideoRecorder.Factory {
    override fun create(config: RecordingConfig): JvmVideoRecorder = FFmpegVideoRecorder(config)
  }
}
