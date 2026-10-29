package com.ujizin.camposer.codescanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.ImageAnalyzer

@Composable
public actual fun CameraSession.rememberCodeImageAnalyzer(
  codeTypes: List<CodeType>,
  onError: (Throwable) -> Unit,
  codeAnalyzerListener: CodeAnalyzerListener,
): ImageAnalyzer =
  remember(this, codeTypes, codeAnalyzerListener) {
    val codeAnalyzer = ImageCodeAnalyzer(
      codeTypes = codeTypes,
      codeAnalyzerListener = codeAnalyzerListener,
      onError = onError,
    )
    ImageAnalyzer(codeAnalyzer::analyze)
  }
