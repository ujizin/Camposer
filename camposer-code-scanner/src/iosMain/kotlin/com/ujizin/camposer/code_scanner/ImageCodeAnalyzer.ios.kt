package com.ujizin.camposer.code_scanner

import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.darwin.NSObject

public actual class ImageCodeAnalyzer actual constructor(
    private val types: List<CodeType>,
    private val codeAnalyzerListener: CodeAnalyzerListener,
) {

    internal val delegate: AVCaptureMetadataOutputObjectsDelegateProtocol =
        object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
            override fun captureOutput(
                output: AVCaptureOutput,
                didOutputMetadataObjects: List<*>,
                fromConnection: AVCaptureConnection,
            ) {
                didOutputMetadataObjects.forEach { obj ->
                    val readable = obj as? AVMetadataMachineReadableCodeObject ?: return@forEach
                    val type = CodeType.findByName(readable.type) ?: return@forEach
                    val text = readable.stringValue ?: return@forEach
                    codeAnalyzerListener.onCodeScanned(CodeResult(type, text))
                }
            }
        }
}
