package com.ujizin.camposer.codescanner

import androidx.compose.ui.geometry.isUnspecified
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.ResultPoint
import com.google.zxing.common.HybridBinarizer
import com.ujizin.camposer.state.properties.CameraFrame
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import kotlin.math.roundToInt

private const val BGR_CHANNELS = 3
private const val LUMA_RED_WEIGHT = 299
private const val LUMA_GREEN_WEIGHT = 587
private const val LUMA_BLUE_WEIGHT = 114
private const val LUMA_DIVISOR = 1000
private const val BYTE_MASK = 0xFF

/**
 * Decodes barcodes with ZXing. One decode per frame; results are reported in preview dp.
 */
internal actual class ImageCodeAnalyzer(
  codeTypes: List<CodeType>,
  private val codeAnalyzerListener: CodeAnalyzerListener,
  private val onError: (Throwable) -> Unit,
) {
  private val reader = MultiFormatReader().apply {
    setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to codeTypes.map { it.format }))
  }

  fun analyze(frame: CameraFrame) {
    val result = decode(frame) ?: return
    val type = CodeType.fromFormat(result.barcodeFormat) ?: return
    val text = result.text ?: return
    val corners = result.resultPoints.orEmpty().map { point -> frame.toCornerPointer(point) }

    codeAnalyzerListener.onCodeScanned(
      CodeResult(
        type = type,
        text = text,
        frameRect = corners.toFrameRect(),
        corners = corners,
      ),
    )
  }

  private fun decode(frame: CameraFrame): Result? =
    try {
      reader.decodeWithState(BinaryBitmap(HybridBinarizer(frame.image.toLuminanceSource())))
    } catch (_: NotFoundException) {
      null
    } catch (
      @Suppress("TooGenericExceptionCaught") error: Exception,
    ) {
      // ZXing throws on corrupt data; the preview must keep running.
      onError(error)
      null
    } finally {
      reader.reset()
    }

  private fun CameraFrame.toCornerPointer(point: ResultPoint): CornerPointer {
    val position = toPreviewPosition(point.x, point.y)
    if (position.isUnspecified) return CornerPointer(point.x.roundToInt(), point.y.roundToInt())
    return CornerPointer(position.x.roundToInt(), position.y.roundToInt())
  }

  private fun List<CornerPointer>.toFrameRect(): FrameRect {
    if (isEmpty()) return FrameRect(0, 0, 0, 0)
    return FrameRect(
      left = minOf { it.x },
      top = minOf { it.y },
      right = maxOf { it.x },
      bottom = maxOf { it.y },
    )
  }
}

internal fun BufferedImage.toLuminanceSource(): LuminanceSource {
  val buffer = raster.dataBuffer
  if (type == BufferedImage.TYPE_3BYTE_BGR && buffer is DataBufferByte) {
    return PlanarYUVLuminanceSource(
      buffer.data.toLuminance(width * height),
      width,
      height,
      0,
      0,
      width,
      height,
      false,
    )
  }

  val pixels = getRGB(0, 0, width, height, null, 0, width)
  return RGBLuminanceSource(width, height, pixels)
}

private fun ByteArray.toLuminance(pixelCount: Int): ByteArray {
  val luminance = ByteArray(pixelCount)
  var offset = 0
  for (index in 0 until pixelCount) {
    val blue = this[offset].toInt() and BYTE_MASK
    val green = this[offset + 1].toInt() and BYTE_MASK
    val red = this[offset + 2].toInt() and BYTE_MASK
    luminance[index] =
      ((red * LUMA_RED_WEIGHT + green * LUMA_GREEN_WEIGHT + blue * LUMA_BLUE_WEIGHT) / LUMA_DIVISOR)
        .toByte()
    offset += BGR_CHANNELS
  }
  return luminance
}
