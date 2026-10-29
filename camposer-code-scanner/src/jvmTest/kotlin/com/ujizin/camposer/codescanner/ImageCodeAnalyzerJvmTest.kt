package com.ujizin.camposer.codescanner

import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.ujizin.camposer.state.properties.CameraFrame
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ImageCodeAnalyzerJvmTest {
  private fun qrImage(
    text: String,
    size: Int = 200,
  ): BufferedImage {
    val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
    val image = BufferedImage(size, size, BufferedImage.TYPE_3BYTE_BGR)
    for (x in 0 until size) {
      for (y in 0 until size) {
        image.setRGB(x, y, if (matrix.get(x, y)) Color.BLACK.rgb else Color.WHITE.rgb)
      }
    }
    return image
  }

  private fun frame(image: BufferedImage) = CameraFrame(image)

  @Test
  fun test_qr_code_is_decoded() {
    var result: CodeResult? = null
    val analyzer = ImageCodeAnalyzer(
      codeTypes = listOf(CodeType.QRCode),
      codeAnalyzerListener = { result = it },
      onError = { throw it },
    )

    analyzer.analyze(frame(qrImage("https://example.com")))

    val code = assertNotNull(result)
    assertEquals(CodeType.QRCode, code.type)
    assertEquals("https://example.com", code.text)
    assertTrue(code.corners.size >= 3)
    assertTrue(code.frameRect.width > 0)
    assertTrue(code.frameRect.height > 0)
  }

  @Test
  fun test_blank_frame_reports_nothing() {
    var result: CodeResult? = null
    var error: Throwable? = null
    val analyzer = ImageCodeAnalyzer(
      codeTypes = listOf(CodeType.QRCode),
      codeAnalyzerListener = { result = it },
      onError = { error = it },
    )

    analyzer.analyze(frame(BufferedImage(64, 64, BufferedImage.TYPE_3BYTE_BGR)))

    assertNull(result)
    assertNull(error)
  }

  @Test
  fun test_code_types_filter_decoding() {
    var result: CodeResult? = null
    val analyzer = ImageCodeAnalyzer(
      codeTypes = listOf(CodeType.BarcodeEAN13),
      codeAnalyzerListener = { result = it },
      onError = {},
    )

    analyzer.analyze(frame(qrImage("hello")))

    assertNull(result)
  }

  @Test
  fun test_rgb_image_falls_back_to_rgb_luminance_source() {
    val source = qrImage("fallback")
    val rgb = BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_RGB).apply {
      graphics.drawImage(source, 0, 0, null)
    }
    var result: CodeResult? = null
    val analyzer = ImageCodeAnalyzer(
      codeTypes = listOf(CodeType.QRCode),
      codeAnalyzerListener = { result = it },
      onError = { throw it },
    )

    analyzer.analyze(frame(rgb))

    assertEquals("fallback", assertNotNull(result).text)
  }
}
