package com.ujizin.camposer.internal.core.applier

import androidx.camera.core.TorchState
import com.ujizin.camposer.internal.core.camerax.CameraXController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.mode
import kotlin.math.roundToInt

internal class ExposureZoomApplier(
  private val cameraState: CameraState,
  private val cameraXController: CameraXController,
) : CameraStateApplier {
  private var hasPaused: Boolean = false

  override fun onCameraInitialized() {
    cameraXController.setZoomRatio(cameraState.zoomRatio.value)
    observeStates()
  }

  private fun observeStates() {
    val owner = cameraXController.lifecycleOwner
    cameraXController.zoomState.observe(owner) {
      cameraState.updateZoomRatio(it.zoomRatio)
    }

    cameraXController.cameraInfo?.torchState?.observe(owner) { state ->
      cameraState.updateTorchEnabled(state == TorchState.ON)
    }
  }

  override fun onCameraResumed() {
    if (!hasPaused) return

    cameraXController.setZoomRatio(cameraState.zoomRatio.value)
    cameraXController.setExposureCompensationIndex(
      cameraState.exposureCompensation.value.roundToInt(),
    )
  }

  override fun onCameraPaused() {
    hasPaused = true
  }

  fun applyFlashMode(flashMode: FlashMode) {
    cameraXController.imageCaptureFlashMode = flashMode.mode
    cameraState.updateFlashMode(flashMode)
  }

  fun applyTorchEnabled(isTorchEnabled: Boolean) {
    cameraXController.enableTorch(isTorchEnabled)
    cameraState.updateTorchEnabled(isTorchEnabled)
  }

  fun applyExposureCompensation(exposureCompensation: Float) {
    cameraXController.setExposureCompensationIndex(exposureCompensation.roundToInt())
    cameraState.updateExposureCompensation(exposureCompensation)
  }

  fun applyZoomRatio(zoomRatio: Float) {
    cameraXController.setZoomRatio(zoomRatio)
    cameraState.updateZoomRatio(zoomRatio)
  }
}
