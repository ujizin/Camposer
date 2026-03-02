package com.ujizin.camposer.shared.camera

import androidx.compose.runtime.Composable
import com.ujizin.camposer.codescanner.CodeType
import com.ujizin.camposer.codescanner.rememberCodeImageAnalyzer
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.shared.utils.QrCorner
import com.ujizin.camposer.shared.utils.QrRect
import com.ujizin.camposer.state.properties.ImageAnalyzer

@Composable
actual fun rememberPlatformCodeAnalyzer(
  cameraSession: CameraSession,
  onDetected: (text: String, rect: QrRect, corners: List<QrCorner>) -> Unit,
): ImageAnalyzer? = cameraSession.rememberCodeImageAnalyzer(
  codeTypes = listOf(CodeType.QRCode),
  onError = { error -> println("Code scanner error: ${error.message}") },
  codeAnalyzerListener = { result ->
    onDetected(
      result.text,
      QrRect(result.frameRect.left, result.frameRect.top, result.frameRect.right, result.frameRect.bottom),
      result.corners.map { QrCorner(it.x, it.y) },
    )
  },
)
