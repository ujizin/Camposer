package com.ujizin.camposer.extensions

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.ujizin.camposer.error.NSDataNotFoundException
import kotlinx.io.files.Path
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL

public actual fun Path.asImageBitmap(): ImageBitmap {
    val fileURL = NSURL.fileURLWithPath(toString())
    val imageData = NSData.dataWithContentsOfURL(fileURL) ?: throw NSDataNotFoundException()

    return Image.makeFromEncoded(imageData.toByteArray()).toComposeImageBitmap()
}

public fun Path.writeData(data: NSData): Boolean {
    val url = NSURL.fileURLWithPath(this.toString())
    return data.writeToURL(url, atomically = true)
}
