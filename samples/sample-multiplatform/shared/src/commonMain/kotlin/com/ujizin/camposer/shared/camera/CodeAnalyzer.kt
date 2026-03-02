package com.ujizin.camposer.shared.camera

import androidx.compose.runtime.Composable
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.shared.utils.QrCorner
import com.ujizin.camposer.shared.utils.QrRect
import com.ujizin.camposer.state.properties.ImageAnalyzer

/**
 * Returns a platform code analyzer that detects QR codes, or null if not supported
 * on this platform (JVM desktop).
 */
@Composable
expect fun rememberPlatformCodeAnalyzer(
  cameraSession: CameraSession,
  onDetected: (text: String, rect: QrRect, corners: List<QrCorner>) -> Unit,
): ImageAnalyzer?
