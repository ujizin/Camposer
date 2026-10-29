package com.ujizin.camposer.shared.utils

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** Desktop has no gallery. Videos are copied into `~/Movies/Camposer`. */
public actual suspend fun FileKit.saveVideoToGallery(file: PlatformFile): Unit =
  withContext(Dispatchers.IO) {
    val source = File(file.path)
    val target = File(moviesDirectory(), file.name)
    Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
  }

public actual suspend fun FileKit.saveVideoToGallery(
  bytes: ByteArray,
  filename: String,
): Unit =
  withContext(Dispatchers.IO) {
    File(moviesDirectory(), filename).writeBytes(bytes)
  }

private fun moviesDirectory(): File =
  File(System.getProperty("user.home"), "Movies/Camposer").apply { mkdirs() }
