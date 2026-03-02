package com.ujizin.camposer.utils

import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.ImageAnalyzer

actual fun createFakeImageAnalyzer(
  cameraSession: CameraSession,
  block: () -> Unit,
): ImageAnalyzer = ImageAnalyzer { _ -> block() }
