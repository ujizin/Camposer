package com.ujizin.camposer.shared.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

public actual suspend fun getFirstFrameVideo(filename: String): ByteArray =
  withContext(Dispatchers.IO) {
    val grabber = FFmpegFrameGrabber(filename)
    try {
      grabber.start()
      val frame = grabber.grabImage() ?: error("Could not extract first frame from video: $filename")
      val image = Java2DFrameConverter().use { converter ->
        converter.convert(frame) ?: error("Could not convert first frame from video: $filename")
      }
      ByteArrayOutputStream().use { output ->
        check(ImageIO.write(image, "jpeg", output)) { "Could not encode the first frame as JPEG." }
        output.toByteArray()
      }
    } finally {
      grabber.stop()
      grabber.release()
    }
  }
