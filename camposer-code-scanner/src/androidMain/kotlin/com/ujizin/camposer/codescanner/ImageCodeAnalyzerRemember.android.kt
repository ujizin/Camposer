package com.ujizin.camposer.codescanner

import android.annotation.SuppressLint
import androidx.camera.core.impl.utils.executor.CameraXExecutors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.ImageAnalyzer

@SuppressLint("RestrictedApi")
@Composable
public actual fun CameraSession.rememberCodeImageAnalyzer(
  codeTypes: List<CodeType>,
  onError: (Throwable) -> Unit,
  codeAnalyzerListener: CodeAnalyzerListener,
): ImageAnalyzer {
  val codeAnalyzer = remember(codeTypes, codeAnalyzerListener) {
    ImageCodeAnalyzer(
      executor = CameraXExecutors.ioExecutor(),
      types = codeTypes,
      codeAnalyzerListener = codeAnalyzerListener,
      onFailure = onError,
    )
  }

  DisposableEffect(codeAnalyzer) { onDispose { codeAnalyzer.release() } }

  return remember(codeAnalyzer) {
    ImageAnalyzer(
      controller = cameraXController,
      analyzer = codeAnalyzer.analyzer,
    )
  }
}
