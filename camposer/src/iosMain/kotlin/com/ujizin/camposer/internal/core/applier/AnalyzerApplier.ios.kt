package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.ImageAnalyzer

internal class AnalyzerApplier(
  private val cameraState: CameraState,
) : CameraStateApplier {
  private var previousAnalyzer: ImageAnalyzer? = cameraState.imageAnalyzer.value

  fun applyImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    disposeImageAnalyzer(previousAnalyzer)
    setImageAnalyzer(imageAnalyzer)
    previousAnalyzer = imageAnalyzer
    cameraState.updateImageAnalyzer(imageAnalyzer)
  }

  fun applyImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean) {
    setImageAnalyzerEnabled(isImageAnalyzerEnabled)
    cameraState.updateImageAnalyzerEnabled(isImageAnalyzerEnabled)
  }

  private fun setImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    imageAnalyzer?.isEnabled = cameraState.isImageAnalyzerEnabled.value
  }

  private fun setImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean) {
    cameraState.imageAnalyzer.value?.isEnabled = isImageAnalyzerEnabled
  }

  private fun disposeImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    imageAnalyzer?.dispose()
  }
}
