package com.ujizin.camposer.shared.utils

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

private fun galleryDir(subdir: String): File {
  val home = System.getProperty("user.home")
  val dir = File(home, "$subdir/Camposer")
  if (!dir.exists()) dir.mkdirs()
  return dir
}

public actual suspend fun FileKit.saveVideoToGallery(file: PlatformFile) {
  val src = File(file.path)
  val dest = File(galleryDir("Movies"), file.name)
  Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
}

public actual suspend fun FileKit.saveVideoToGallery(
  bytes: ByteArray,
  filename: String,
) {
  File(galleryDir("Movies"), filename).writeBytes(bytes)
}
