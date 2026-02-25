package com.ujizin.camposer.utils

import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.ImageAnalyzer
import platform.AVFoundation.AVCaptureMetadataOutput

actual fun createFakeImageAnalyzer(
  cameraSession: CameraSession,
  block: () -> Unit,
): ImageAnalyzer =
  ImageAnalyzer(
    controller = cameraSession.iosCameraController,
    analyzer = ImageAnalyzer.Analyzer(
      onOutputAttached = { block() },
      output = AVCaptureMetadataOutput(),
    ),
  )
