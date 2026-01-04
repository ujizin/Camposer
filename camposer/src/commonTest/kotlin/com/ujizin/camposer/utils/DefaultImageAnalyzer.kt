package com.ujizin.camposer.utils

import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.ImageAnalyzer

expect fun createFakeImageAnalyzer(
  cameraSession: CameraSession,
  block: () -> Unit,
): ImageAnalyzer
