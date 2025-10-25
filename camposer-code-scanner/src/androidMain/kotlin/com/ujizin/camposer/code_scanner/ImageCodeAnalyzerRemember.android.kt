package com.ujizin.camposer.code_scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ujizin.camposer.code_scanner.model.CodeType
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.ImageAnalyzer

@Composable
public actual fun CameraSession.rememberCodeImageAnalyzer(
    codeTypes: List<CodeType>,
    onError: (Throwable) -> Unit,
    codeAnalyzerListener: CodeAnalyzerListener,
): ImageAnalyzer = remember(codeTypes, codeAnalyzerListener) {
    ImageAnalyzer(
        controller = cameraXController,
        analyzer = ImageCodeAnalyzer(codeTypes, codeAnalyzerListener, onError),
    )
}
