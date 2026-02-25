package com.ujizin.camposer.shared.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import java.io.ByteArrayOutputStream

public actual suspend fun getFirstFrameVideo(filename: String): ByteArray {
  val retriever = MediaMetadataRetriever()
  return try {
    retriever.setDataSource(filename)
    val firstFrameBitmap = retriever.getFrameAtTime(
      0L,
      MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
    ) ?: error("Could not extract first frame from video: $filename")

    firstFrameBitmap.toJpegByteArray()
  } finally {
    retriever.release()
  }
}

private fun Bitmap.toJpegByteArray(): ByteArray =
  ByteArrayOutputStream().use { outputStream ->
    check(compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
      "Could not encode the first frame as JPEG."
    }
    outputStream.toByteArray()
  }
