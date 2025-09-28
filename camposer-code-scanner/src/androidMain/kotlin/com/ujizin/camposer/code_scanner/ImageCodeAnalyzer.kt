package com.ujizin.camposer.code_scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.ujizin.camposer.code_scanner.model.CodeResult
import com.ujizin.camposer.code_scanner.model.CodeType

public actual class ImageCodeAnalyzer actual constructor(
    private val types: List<CodeType>,
    private val codeAnalyzerListener: CodeAnalyzerListener,
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {

        // TODO add mlkit

        codeAnalyzerListener.onCodeScanned(
            CodeResult(
                type = CodeType.QRCode,
                text = "Hello world!"
            )
        )
    }
}