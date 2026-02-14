package com.ujizin.camposer.shared.utils

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes

public suspend fun FileKit.saveVideoToGallery(
  file: PlatformFile,
) = saveVideoToGallery(
  bytes = file.readBytes(),
  filename = file.name,
)

public expect suspend fun FileKit.saveVideoToGallery(
  bytes: ByteArray,
  filename: String,
)