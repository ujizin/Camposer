package com.ujizin.camposer.internal.extensions

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.bytedeco.opencv.opencv_core.Mat
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte

internal fun Mat.toImageBitmap(): ImageBitmap {
  val width = cols()
  val height = rows()
  val channels = channels()
  val data = ByteArray(width * height * channels)
  data().get(data)
  // OpenCV outputs BGR, which matches BufferedImage.TYPE_3BYTE_BGR — no color conversion needed.
  val image = BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
  (image.raster.dataBuffer as DataBufferByte).data.let { data.copyInto(it) }
  return image.toComposeImageBitmap()
}
