package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.selector.CamSelector

internal actual class SessionTopologyApplier(
  private val cameraState: CameraState,
) : CameraStateApplier {
  fun applyCaptureMode(captureMode: CaptureMode) {
    cameraState.updateCaptureMode(captureMode)
  }

  fun applyCamSelector(camSelector: CamSelector) {
    cameraState.updateCamSelector(camSelector)
  }

  fun applyCamFormat(camFormat: CamFormat) {
    cameraState.updateCamFormat(camFormat)
  }
}
