package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.internal.capture.JvmCameraCapture
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_EXPOSURE
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_ZOOM

internal actual class ExposureZoomApplier(
  private val cameraState: CameraState,
  private val capture: JvmCameraCapture,
) : CameraStateApplier {
  actual fun applyFlashMode(flashMode: FlashMode) {
    cameraState.updateFlashMode(flashMode)
  }

  actual fun applyTorchEnabled(isTorchEnabled: Boolean) {
    cameraState.updateTorchEnabled(isTorchEnabled)
  }

  actual fun applyExposureCompensation(exposureCompensation: Float) {
    capture.set(CAP_PROP_EXPOSURE, exposureCompensation.toDouble())
    cameraState.updateExposureCompensation(exposureCompensation)
  }

  actual fun applyZoomRatio(zoomRatio: Float) {
    capture.set(CAP_PROP_ZOOM, zoomRatio.toDouble())
    cameraState.updateZoomRatio(zoomRatio)
  }
}
