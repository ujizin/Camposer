package com.ujizin.camposer.shared.utils

import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

public actual suspend fun getFirstFrameVideo(filename: String): ByteArray {
  val grabber = FFmpegFrameGrabber(filename)
  grabber.start()
  return try {
    val converter = Java2DFrameConverter()
    val frame = grabber.grabImage()
    val buffered = converter.convert(frame)
      ?: return ByteArray(0)
    val out = ByteArrayOutputStream()
    ImageIO.write(buffered, "jpeg", out)
    out.toByteArray()
  } finally {
    grabber.stop()
  }
}
