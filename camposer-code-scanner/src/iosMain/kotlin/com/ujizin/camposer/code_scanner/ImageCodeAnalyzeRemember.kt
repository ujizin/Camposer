package com.ujizin.camposer.code_scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.ujizin.camposer.code_scanner.CodeScannerQueue.codeAnalyzerQueue
import com.ujizin.camposer.code_scanner.model.CodeType
import com.ujizin.camposer.code_scanner.model.CodeTypeNotSupportedException
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.ImageAnalyzer
import platform.AVFoundation.AVCaptureMetadataOutput

@Composable
public actual fun CameraSession.rememberCodeImageAnalyzer(
    codeTypes: List<CodeType>,
    onError: (Throwable) -> Unit,
    codeAnalyzerListener: CodeAnalyzerListener,
): ImageAnalyzer {
    val codeAnalyzer = remember(codeTypes, codeAnalyzerListener) {
        val codeAnalyzerDelegate = ImageCodeAnalyzer(codeTypes, codeAnalyzerListener)
        ImageAnalyzer(
            iosCameraSession = iosCameraSession,
            analyzer = ImageAnalyzer.Analyzer(
                output = AVCaptureMetadataOutput(),
                onOutputAttached = { output ->
                    output.setMetadataObjectsDelegate(codeAnalyzerDelegate, codeAnalyzerQueue)
                    val supportedTypes = codeTypes.mapNotNull { type ->
                        val isSupported = output.availableMetadataObjectTypes.contains(type.rawType)
                        if (!isSupported) {
                            onError(CodeTypeNotSupportedException(type))
                            return@mapNotNull null
                        }

                        type.rawType
                    }
                    output.metadataObjectTypes = supportedTypes
                }
            ),
        )
    }

    DisposableEffect(codeAnalyzer) {
        onDispose { codeAnalyzer.onDispose() }
    }

    return codeAnalyzer
}
