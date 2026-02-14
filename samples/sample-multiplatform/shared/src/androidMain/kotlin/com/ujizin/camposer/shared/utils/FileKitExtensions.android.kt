package com.ujizin.camposer.shared.utils

import android.content.ContentValues
import android.provider.MediaStore
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public actual suspend fun FileKit.saveVideoToGallery(
  bytes: ByteArray,
  filename: String,
): Unit = withContext(Dispatchers.IO) {
  val collection =
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
      MediaStore.Video.Media.getContentUri(
        MediaStore.VOLUME_EXTERNAL_PRIMARY
      )
    } else {
      MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }

  val imageDetails = ContentValues().apply {
    put(MediaStore.Video.Media.DISPLAY_NAME, filename)
  }

  val resolver = context.contentResolver
  resolver.insert(collection, imageDetails)?.let { imageUri ->
    resolver.openOutputStream(imageUri)?.use { it.write(bytes + ByteArray(1)) }
  }
}