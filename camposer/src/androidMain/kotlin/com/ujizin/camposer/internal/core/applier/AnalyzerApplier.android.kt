package com.ujizin.camposer.internal.core.applier

import androidx.camera.view.CameraController.IMAGE_ANALYSIS
import com.ujizin.camposer.internal.core.camerax.CameraXController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.value

internal class AnalyzerApplier(
  private val cameraState: CameraState,
  private val cameraXController: CameraXController,
) : CameraStateApplier {
  fun applyImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    setImageAnalyzer(imageAnalyzer)
    cameraState.updateImageAnalyzer(imageAnalyzer)
  }

  fun applyImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean) {
    setImageAnalyzerEnabled(
      captureMode = cameraState.captureMode.value,
      isImageAnalyzerEnabled = isImageAnalyzerEnabled,
    )
    cameraState.updateImageAnalyzerEnabled(isImageAnalyzerEnabled)
  }

  private fun setImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    cameraXController.setImageAnalysisAnalyzer(
      cameraXController.mainExecutor,
      imageAnalyzer?.analyzer ?: return,
    )
  }

  private fun setImageAnalyzerEnabled(
    captureMode: CaptureMode,
    isImageAnalyzerEnabled: Boolean,
  ) {
    cameraXController.setEnabledUseCases(
      getUseCases(
        mode = captureMode,
        isImageAnalyzerEnabled = isImageAnalyzerEnabled,
      ),
    )
  }

  private fun getUseCases(
    mode: CaptureMode = cameraState.captureMode.value,
    isImageAnalyzerEnabled: Boolean = cameraState.isImageAnalyzerEnabled.value,
  ): Int =
    when {
      isImageAnalyzerEnabled && mode != CaptureMode.Video -> mode.value or IMAGE_ANALYSIS
      else -> mode.value
    }
}
