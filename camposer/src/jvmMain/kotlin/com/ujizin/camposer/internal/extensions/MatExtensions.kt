package com.ujizin.camposer.internal.extensions

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.Mat
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte

internal fun Mat.toImageBitmap(): ImageBitmap {
  val rgbMat = Mat()
  opencv_imgproc.cvtColor(this, rgbMat, opencv_imgproc.COLOR_BGR2RGB)
  val width = rgbMat.cols()
  val height = rgbMat.rows()
  val channels = rgbMat.channels()
  val data = ByteArray(width * height * channels)
  rgbMat.data().get(data)
  val image = BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
  (image.raster.dataBuffer as DataBufferByte).data.let { data.copyInto(it) }
  return image.toComposeImageBitmap()
}
