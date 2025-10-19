package com.ujizin.camposer.code_scanner

import androidx.compose.runtime.Composable
import com.ujizin.camposer.code_scanner.model.CodeType
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.ImageAnalyzer

@Composable
public expect fun CameraSession.rememberCodeImageAnalyzer(
    codeTypes: List<CodeType> = listOf(CodeType.QRCode),
    onError: (Throwable) -> Unit = {},
    codeAnalyzerListener: CodeAnalyzerListener,
): ImageAnalyzer