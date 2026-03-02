package com.ujizin.camposer.shared.camera

import androidx.compose.runtime.Composable
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.shared.utils.QrCorner
import com.ujizin.camposer.shared.utils.QrRect
import com.ujizin.camposer.state.properties.ImageAnalyzer

@Composable
actual fun rememberPlatformCodeAnalyzer(
  cameraSession: CameraSession,
  onDetected: (text: String, rect: QrRect, corners: List<QrCorner>) -> Unit,
): ImageAnalyzer? = null
