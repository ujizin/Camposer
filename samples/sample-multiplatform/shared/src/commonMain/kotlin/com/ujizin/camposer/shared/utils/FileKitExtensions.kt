package com.ujizin.camposer.shared.utils

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile

public expect suspend fun FileKit.saveVideoToGallery(file: PlatformFile)

public expect suspend fun FileKit.saveVideoToGallery(
  bytes: ByteArray,
  filename: String,
)
