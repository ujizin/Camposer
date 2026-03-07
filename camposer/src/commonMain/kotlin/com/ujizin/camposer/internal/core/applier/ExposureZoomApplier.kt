package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.state.properties.FlashMode

internal expect class ExposureZoomApplier : CameraStateApplier {
  fun applyFlashMode(flashMode: FlashMode)
  fun applyExposureCompensation(exposureCompensation: Float)
  fun applyZoomRatio(zoomRatio: Float)
  fun applyTorchEnabled(isTorchEnabled: Boolean)
}
