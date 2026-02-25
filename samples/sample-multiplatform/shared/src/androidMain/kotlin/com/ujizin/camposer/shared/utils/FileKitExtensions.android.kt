package com.ujizin.camposer.shared.utils

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.context
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

public actual suspend fun FileKit.saveVideoToGallery(
  file: PlatformFile,
): Unit = withContext(Dispatchers.IO) {
  val input = openInputStream(file) ?: return@withContext
  writeVideoToGallery(filename = file.name) { output ->
    input.use { it.copyTo(output) }
  }
}

public actual suspend fun FileKit.saveVideoToGallery(
  bytes: ByteArray,
  filename: String,
): Unit = withContext(Dispatchers.IO) {
  writeVideoToGallery(filename = filename) { output ->
    output.write(bytes)
  }
}

private fun FileKit.writeVideoToGallery(
  filename: String,
  writer: (OutputStream) -> Unit,
) {
  val resolver = context.contentResolver
  val videoUri = resolver.insert(getVideoCollection(), createVideoDetails(filename)) ?: return

  try {
    resolver.openOutputStream(videoUri)?.use(writer) ?: resolver.delete(videoUri, null, null)
  } catch (throwable: Throwable) {
    resolver.delete(videoUri, null, null)
    throw throwable
  }
}

private fun FileKit.openInputStream(file: PlatformFile): InputStream? =
  when (val source = file.androidFile) {
    is AndroidFile.FileWrapper -> source.file.inputStream()
    is AndroidFile.UriWrapper -> context.contentResolver.openInputStream(source.uri)
  }

private fun getVideoCollection(): Uri =
  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
  } else {
    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
  }

private fun createVideoDetails(filename: String): ContentValues = ContentValues().apply {
  put(MediaStore.Video.Media.DISPLAY_NAME, filename)
}
