package com.ujizin.camposer.code_scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.ujizin.camposer.code_scanner.CodeScannerQueue.codeAnalyzerQueue
import com.ujizin.camposer.code_scanner.model.CodeType
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageAnalyzer
import platform.AVFoundation.AVCaptureMetadataOutput

@Composable
public actual fun CameraState.rememberCodeImageAnalyzer(
    codeTypes: List<CodeType>,
    codeAnalyzerListener: CodeAnalyzerListener,
): ImageAnalyzer {
    val codeAnalyzer = remember(codeTypes, codeAnalyzerListener) {
        val codeAnalyzerDelegate = ImageCodeAnalyzer(codeTypes, codeAnalyzerListener)
        ImageAnalyzer(
            controller = controller,
            analyzer = ImageAnalyzer.Analyzer(
                output = AVCaptureMetadataOutput(),
                onOutputAttached = { output ->
                    output.setMetadataObjectsDelegate(codeAnalyzerDelegate, codeAnalyzerQueue)
                    val codeTypes = codeTypes.flatMap { type ->
                        type.rawType.filter { output.availableMetadataObjectTypes.contains(it) }
                    }
                    output.metadataObjectTypes = codeTypes
                }
            ),
        )
    }

    DisposableEffect(codeAnalyzer) {
        onDispose { codeAnalyzer.onDispose() }
    }

    return codeAnalyzer
}
