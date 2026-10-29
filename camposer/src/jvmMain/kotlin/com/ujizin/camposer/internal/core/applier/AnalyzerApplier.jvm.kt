package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.internal.core.jvm.JvmCameraController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.ImageAnalyzer

internal actual class AnalyzerApplier(
  private val cameraState: CameraState,
  private val jvmCameraController: JvmCameraController,
) : CameraStateApplier {
  actual fun applyImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    val isEnabled = cameraState.isImageAnalyzerEnabled.value
    jvmCameraController.setFrameAnalyzer(imageAnalyzer?.analyzer?.takeIf { isEnabled })
    cameraState.updateImageAnalyzer(imageAnalyzer)
  }

  actual fun applyImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean) {
    val analyzer = cameraState.imageAnalyzer.value?.analyzer
    jvmCameraController.setFrameAnalyzer(analyzer?.takeIf { isImageAnalyzerEnabled })
    cameraState.updateImageAnalyzerEnabled(isImageAnalyzerEnabled)
  }
}
