package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.ImageAnalyzer

internal actual class AnalyzerApplier(
  private val cameraState: CameraState,
) : CameraStateApplier {
  fun applyImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    cameraState.updateImageAnalyzer(imageAnalyzer)
  }

  fun applyImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean) {
    cameraState.updateImageAnalyzerEnabled(isImageAnalyzerEnabled)
  }
}
