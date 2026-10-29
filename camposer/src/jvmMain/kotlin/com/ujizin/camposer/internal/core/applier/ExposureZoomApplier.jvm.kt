package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.internal.core.jvm.JvmCameraController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode

/**
 * Desktop webcams have no flash, torch or optical zoom. Those values are stored in state only;
 * [com.ujizin.camposer.info.CameraInfo] reports them unsupported so the controller rejects them.
 */
internal actual class ExposureZoomApplier(
  private val cameraState: CameraState,
  private val jvmCameraController: JvmCameraController,
) : CameraStateApplier {
  actual fun applyFlashMode(flashMode: FlashMode) {
    cameraState.updateFlashMode(flashMode)
  }

  actual fun applyTorchEnabled(isTorchEnabled: Boolean) {
    cameraState.updateTorchEnabled(isTorchEnabled)
  }

  // Runs off the caller thread: the hardware write waits for the capture lock.
  actual fun applyExposureCompensation(exposureCompensation: Float) {
    cameraState.launch {
      jvmCameraController.setExposureCompensation(exposureCompensation)
      cameraState.updateExposureCompensation(exposureCompensation)
    }
  }

  actual fun applyZoomRatio(zoomRatio: Float) {
    cameraState.updateZoomRatio(zoomRatio)
  }
}
