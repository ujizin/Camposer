package com.ujizin.sample.extensions

import androidx.annotation.WorkerThread
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    val data = ByteArray(remaining())
    get(data)
    return data
}

@WorkerThread
suspend fun MultiFormatReader.getQRCodeResult(
    image: ImageProxy,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) = withContext(dispatcher) {
    with(image) {
        val data = planes.firstOrNull()?.buffer?.toByteArray()
        val source = PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            decode(binaryBitmap)
        } catch (e: NotFoundException) {
            e.printStackTrace()
            null
        }
    }
}