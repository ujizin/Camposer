package com.ujizin.camposer.internal.core.jvm

import org.bytedeco.javacpp.BytePointer
import org.bytedeco.opencv.global.opencv_core.CV_8UC3
import org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR
import org.bytedeco.opencv.global.opencv_imgcodecs.imdecode
import org.bytedeco.opencv.opencv_core.Mat
import java.awt.image.DataBufferByte
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Exercises the real OpenCV conversions on a synthetic frame; no camera is needed. */
internal class MatFrameTest {
  // 2x1 BGR image: left pixel pure blue, right pixel pure red.
  private fun createMat(): Mat =
    Mat(1, 2, CV_8UC3).also { mat ->
      mat.data().put(byteArrayOf(-1, 0, 0, 0, 0, -1), 0, 6)
    }

  @Test
  fun test_size_matches_mat() {
    MatFrame(createMat()).use { frame ->
      assertEquals(2, frame.width)
      assertEquals(1, frame.height)
    }
  }

  @Test
  fun test_to_buffered_image_keeps_bgr_bytes() {
    MatFrame(createMat()).use { frame ->
      val image = frame.toBufferedImage()

      val bytes = (image.raster.dataBuffer as DataBufferByte).data
      assertContentEquals(byteArrayOf(-1, 0, 0, 0, 0, -1), bytes)
      assertEquals(0xFF0000FF.toInt(), image.getRGB(0, 0))
      assertEquals(0xFFFF0000.toInt(), image.getRGB(1, 0))
    }
  }

  @Test
  fun test_to_image_bitmap_has_frame_size() {
    MatFrame(createMat()).use { frame ->
      val bitmap = frame.toImageBitmap()

      assertEquals(2, bitmap.width)
      assertEquals(1, bitmap.height)
    }
  }

  @Test
  fun test_to_recorder_frame_keeps_size_and_mirrors() {
    MatFrame(createMat()).use { frame ->
      val plain = frame.toRecorderFrame(isMirrored = false)
      assertEquals(2, plain.imageWidth)
      assertEquals(1, plain.imageHeight)

      val mirrored = frame.toRecorderFrame(isMirrored = true)
      assertEquals(2, mirrored.imageWidth)
    }
  }

  @Test
  fun test_encode_jpeg_produces_decodable_image() {
    MatFrame(createMat()).use { frame ->
      val bytes = frame.encodeJpeg(quality = 90, isMirrored = false)

      assertTrue(bytes.size > 2)
      assertEquals(0xFF.toByte(), bytes[0])
      assertEquals(0xD8.toByte(), bytes[1])
      val decoded = imdecode(Mat(BytePointer(*bytes)), IMREAD_COLOR)
      assertEquals(2, decoded.cols())
      assertEquals(1, decoded.rows())
    }
  }

  @Test
  fun test_to_recorder_frame_mirrored_swaps_pixels() {
    MatFrame(createMat()).use { frame ->
      val mirrored = frame.toRecorderFrame(isMirrored = true)

      val buffer = mirrored.image[0] as ByteBuffer
      val pixels = ByteArray(6).also {
        buffer.rewind()
        buffer.get(it)
      }
      // After mirroring the red pixel is on the left: BGR = (0, 0, 255), then blue.
      assertContentEquals(byteArrayOf(0, 0, -1, -1, 0, 0), pixels)
    }
  }
}
