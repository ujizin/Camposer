package com.ujizin.camposer.code_scanner

import com.ujizin.camposer.code_scanner.model.CodeResult
import com.ujizin.camposer.code_scanner.model.CodeType

public fun interface CodeAnalyzerListener {
    public fun onCodeScanned(result: CodeResult)
}

public expect class ImageCodeAnalyzer(
    types: List<CodeType> = listOf(CodeType.QRCode),
    codeAnalyzerListener: CodeAnalyzerListener,
)