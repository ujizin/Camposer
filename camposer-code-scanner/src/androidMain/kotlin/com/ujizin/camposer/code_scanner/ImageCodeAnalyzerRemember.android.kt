package com.ujizin.camposer.code_scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.ImageAnalyzer

@Composable
public actual fun CameraSession.rememberCodeImageAnalyzer(
    codeTypes: List<CodeType>,
    onError: (Throwable) -> Unit,
    codeAnalyzerListener: CodeAnalyzerListener,
): ImageAnalyzer {
    val codeAnalyzer = remember(codeTypes, codeAnalyzerListener) {
        ImageCodeAnalyzer(mainExecutor, codeTypes, codeAnalyzerListener, onError)
    }

    DisposableEffect(codeAnalyzer) {
        onDispose { codeAnalyzer.release() }
    }

    return remember(codeAnalyzer) {
        ImageAnalyzer(
            controller = cameraXController,
            analyzer = codeAnalyzer.analyzer,
        )
    }
}
