package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.mode

internal class ExposureZoomApplier(
  private val cameraState: CameraState,
  private val iOSCameraController: IOSCameraController,
) : CameraStateApplier {
  fun applyFlashMode(flashMode: FlashMode) {
    iOSCameraController.setFlashMode(flashMode.mode)
    cameraState.updateFlashMode(flashMode)
  }

  fun applyTorchEnabled(isTorchEnabled: Boolean) {
    iOSCameraController.setTorchEnabled(isTorchEnabled)
    cameraState.updateTorchEnabled(isTorchEnabled)
  }

  fun applyExposureCompensation(exposureCompensation: Float) {
    iOSCameraController.setExposureCompensation(exposureCompensation)
    cameraState.updateExposureCompensation(exposureCompensation)
  }

  fun applyZoomRatio(zoomRatio: Float) {
    iOSCameraController.setZoomRatio(zoomRatio)
    cameraState.updateZoomRatio(zoomRatio)
  }
}
