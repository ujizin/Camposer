package com.ujizin.camposer.code_scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ujizin.camposer.code_scanner.model.CodeType
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageAnalyzer

@Composable
public actual fun CameraState.rememberCodeImageAnalyzer(
    codeTypes: List<CodeType>,
    onError: (Throwable) -> Unit,
    codeAnalyzerListener: CodeAnalyzerListener,
): ImageAnalyzer = remember(codeTypes, codeAnalyzerListener) {
    ImageAnalyzer(
        cameraState = this,
        analyzer = ImageCodeAnalyzer(codeTypes, codeAnalyzerListener, onError),
    )
}
