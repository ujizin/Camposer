package com.ujizin.camposer.code_scanner

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

public actual class ImageCodeAnalyzer actual constructor(
    private val types: List<CodeType>,
    private val codeAnalyzerListener: CodeAnalyzerListener,
) {

    private var onFailure: (Throwable) -> Unit = {}

    internal constructor(
        types: List<CodeType>,
        codeAnalyzerListener: CodeAnalyzerListener,
        onError: (Throwable) -> Unit,
    ) : this(types, codeAnalyzerListener) {
        onFailure = onError
    }

    private val barcodeScanner = BarcodeScanning.getClient(
        /* options = */ BarcodeScannerOptions.Builder().run {
            val format = types.firstOrNull()?.barcode ?: Barcode.FORMAT_UNKNOWN
            val moreFormats = types.drop(1).map { it.barcode }.toIntArray()
            setBarcodeFormats(format, *moreFormats)
        }.build()
    )

    internal val analyzer = object : ImageAnalysis.Analyzer {
        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image ?: return
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes -> barcodes.forEach(::onCodeScanned) }
                .addOnFailureListener(onFailure)
                .addOnCompleteListener { imageProxy.close() }
        }
    }

    private fun onCodeScanned(barcodeResult: Barcode) {
        val text = barcodeResult.rawValue ?: return
        val format = CodeType.fromBarcode(barcodeResult.format) ?: return
        codeAnalyzerListener.onCodeScanned(
            result = CodeResult(type = format, text = text)
        )
    }
}