package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.state.properties.ImageAnalyzer

internal expect class AnalyzerApplier : CameraStateApplier {
  fun applyImageAnalyzer(imageAnalyzer: ImageAnalyzer?)

  fun applyImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean)
}
