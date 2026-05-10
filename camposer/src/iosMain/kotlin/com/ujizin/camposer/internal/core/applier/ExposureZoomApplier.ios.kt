package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.mode

internal actual class ExposureZoomApplier(
  private val cameraState: CameraState,
  private val iOSCameraController: IOSCameraController,
) : CameraStateApplier {
  actual fun applyFlashMode(flashMode: FlashMode) {
    iOSCameraController.setFlashMode(flashMode.mode)
    cameraState.updateFlashMode(flashMode)
  }

  actual fun applyTorchEnabled(isTorchEnabled: Boolean) {
    iOSCameraController.setTorchEnabled(isTorchEnabled)
    cameraState.updateTorchEnabled(isTorchEnabled)
  }

  actual fun applyExposureCompensation(exposureCompensation: Float) {
    iOSCameraController.setExposureCompensation(exposureCompensation)
    cameraState.updateExposureCompensation(exposureCompensation)
  }

  actual fun applyZoomRatio(zoomRatio: Float) {
    iOSCameraController.setZoomRatio(zoomRatio)
    cameraState.updateZoomRatio(zoomRatio)
  }
}
