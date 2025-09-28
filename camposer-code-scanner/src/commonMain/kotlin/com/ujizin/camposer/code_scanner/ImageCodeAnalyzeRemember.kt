package com.ujizin.camposer.code_scanner

import androidx.compose.runtime.Composable
import com.ujizin.camposer.code_scanner.model.CodeType
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageAnalyzer

@Composable
public expect fun CameraState.rememberCodeImageAnalyzer(
    codeTypes: List<CodeType> = listOf(CodeType.QRCode),
    codeAnalyzerListener: CodeAnalyzerListener,
): ImageAnalyzer