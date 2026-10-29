package com.ujizin.camposer.internal.record

import com.ujizin.camposer.internal.core.jvm.MatFrame
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.opencv.global.opencv_core.CV_8UC3
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Scalar
import java.io.File
import java.nio.ShortBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Encodes synthetic frames through the real FFmpeg pipeline; no camera or microphone needed. */
internal class FFmpegVideoRecorderTest {
  private fun recordTo(
    file: File,
    audioChannels: Int,
    frames: Int = 10,
  ) {
    val config = RecordingConfig(
      filename = file.path,
      width = 64,
      height = 48,
      frameRate = 10.0,
      audioChannels = audioChannels,
      sampleRate = 44_100,
    )
    val recorder = FFmpegVideoRecorder.create(config)
    val mat = Mat(48, 64, CV_8UC3, Scalar(0.0, 128.0, 255.0, 0.0))
    MatFrame(mat).use { frame ->
      recorder.start()
      repeat(frames) { index ->
        recorder.recordFrame(frame, isMirrored = index % 2 == 0, timestampMicros = index * 100_000L)
        if (audioChannels > 0) {
          recorder.recordSamples(ShortBuffer.wrap(ShortArray(4_410) { (it % 100).toShort() }))
        }
      }
      recorder.stop()
    }
  }

  @Test
  fun test_records_video_with_audio_to_mp4() {
    val file = File.createTempFile("camposer", ".mp4").apply { delete() }
    try {
      recordTo(file, audioChannels = 1)

      assertTrue(file.length() > 0)
      FFmpegFrameGrabber(file).use { grabber ->
        grabber.start()
        assertEquals(64, grabber.imageWidth)
        assertEquals(48, grabber.imageHeight)
        assertEquals(1, grabber.audioChannels)
        assertTrue(grabber.lengthInVideoFrames >= 8, "expected at least 8 frames")
        grabber.stop()
      }
    } finally {
      file.delete()
    }
  }

  @Test
  fun test_records_video_only_to_mov() {
    val file = File.createTempFile("camposer", ".mov").apply { delete() }
    try {
      recordTo(file, audioChannels = 0)

      assertTrue(file.length() > 0)
      FFmpegFrameGrabber(file).use { grabber ->
        grabber.start()
        assertEquals(64, grabber.imageWidth)
        assertEquals(0, grabber.audioChannels)
        grabber.stop()
      }
    } finally {
      file.delete()
    }
  }
}
