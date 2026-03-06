package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.internal.capture.JvmCameraCapture
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.ImageAnalyzer
import org.bytedeco.opencv.opencv_core.Mat

internal actual class AnalyzerApplier(
  private val cameraState: CameraState,
  private val capture: JvmCameraCapture,
) : CameraStateApplier {
  private val frameListener: (Mat) -> Unit = { mat ->
    cameraState.imageAnalyzer.value?.analyze(mat)
  }

  fun applyImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    cameraState.updateImageAnalyzer(imageAnalyzer)
  }

  fun applyImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean) {
    if (isImageAnalyzerEnabled) {
      capture.addFrameListener(frameListener)
    } else {
      capture.removeFrameListener(frameListener)
    }
    cameraState.updateImageAnalyzerEnabled(isImageAnalyzerEnabled)
  }
}
