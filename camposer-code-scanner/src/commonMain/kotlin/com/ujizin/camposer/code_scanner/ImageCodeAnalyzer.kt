package com.ujizin.camposer.code_scanner

public fun interface CodeAnalyzerListener {
    public fun onCodeScanned(result: CodeResult)
}

public expect class ImageCodeAnalyzer(
    types: List<CodeType> = listOf(CodeType.QRCode),
    codeAnalyzerListener: CodeAnalyzerListener,
)