package com.ujizin.camposer.extensions

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.io.files.Path
import java.io.File

internal fun Path.toFile() = File(name)

internal fun Uri.toPath() = Path(path ?: throw RuntimeException("Uri must be a file"))

public actual fun Path.asImageBitmap(): ImageBitmap {
    return BitmapFactory.decodeFile(this.toString()).asImageBitmap()
}