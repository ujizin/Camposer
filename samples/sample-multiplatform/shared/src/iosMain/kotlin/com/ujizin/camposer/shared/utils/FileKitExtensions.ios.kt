package com.ujizin.camposer.shared.utils

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSUUID
import platform.Foundation.NSURL
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHPhotoLibrary
import kotlinx.io.files.Path
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual suspend fun FileKit.saveVideoToGallery(file: PlatformFile) {
  val fileUrl = NSURL.fileURLWithPath(file.path)
  saveVideoToGallery(fileUrl = fileUrl)
}

actual suspend fun FileKit.saveVideoToGallery(
  bytes: ByteArray,
  filename: String,
) {
  val tempFile = PlatformFile(
    Path("$SystemTemporaryDirectory/$filename-${NSUUID().UUIDString}.tmp").toString(),
  )

  try {
    tempFile write bytes
    saveVideoToGallery(file = tempFile)
  } finally {
    runCatching {
      tempFile.delete(mustExist = false)
    }
  }
}

private suspend fun saveVideoToGallery(fileUrl: NSURL): Unit = suspendCancellableCoroutine {
  continuation ->
  PHPhotoLibrary.sharedPhotoLibrary().performChanges(
    changeBlock = {
      PHAssetChangeRequest.creationRequestForAssetFromVideoAtFileURL(fileUrl)
    },
    completionHandler = { success, error ->
      if (success) {
        continuation.resume(Unit)
      } else {
        val message = error?.localizedDescription ?: "Failed to save video to gallery"
        continuation.resumeWithException(IllegalStateException(message))
      }
    },
  )
}
