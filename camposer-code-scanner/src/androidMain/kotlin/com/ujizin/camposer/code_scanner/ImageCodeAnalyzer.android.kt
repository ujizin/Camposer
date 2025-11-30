package com.ujizin.camposer.code_scanner

import android.content.res.Resources
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.Executor

internal actual class ImageCodeAnalyzer(
    executor: Executor,
    private val types: List<CodeType>,
    private val codeAnalyzerListener: CodeAnalyzerListener,
    private val onFailure: (Throwable) -> Unit,
) {

    private val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()

    private val barcodeScanner = BarcodeScanning.getClient(
        /* options = */ BarcodeScannerOptions.Builder().run {
            val format = types.firstOrNull()?.barcode ?: Barcode.FORMAT_UNKNOWN
            val moreFormats = types.drop(1).map { it.barcode }.toIntArray()
            setBarcodeFormats(format, *moreFormats)
        }.build()
    )

    internal val analyzer: ImageAnalysis.Analyzer = MlKitAnalyzer(
        listOf(barcodeScanner),
        ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
        executor,
    ) { result ->
        val barcodeResult = result.getValue(barcodeScanner) ?: return@MlKitAnalyzer
        barcodeResult.forEach { barcode ->
            val codeResult = getCodeResult(barcode)
            codeResult?.let(codeAnalyzerListener::onCodeScanned)
        }
    }

    private fun getCodeResult(barcodeResult: Barcode): CodeResult? {
        val text = barcodeResult.rawValue ?: return null
        val format = CodeType.fromBarcode(barcodeResult.format) ?: return null
        val boundingBox = barcodeResult.boundingBox ?: Rect(0, 0, 0, 0)
        val corners = barcodeResult.cornerPoints?.map {
            CornerPointer(it.x.dp, it.y.dp)
        }.orEmpty()

        return CodeResult(
            type = format,
            text = text,
            frameRect = FrameRect(
                left = boundingBox.left.dp,
                top = boundingBox.top.dp,
                right = boundingBox.right.dp,
                bottom = boundingBox.bottom.dp,
            ),
            corners = corners
        )
    }

    internal fun release() {
        barcodeScanner.close()
    }
}
