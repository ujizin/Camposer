package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.selector.CamSelector

internal expect class SessionTopologyApplier : CameraStateApplier {
  fun applyCaptureMode(captureMode: CaptureMode)

  fun applyCamSelector(camSelector: CamSelector)

  fun applyCamFormat(camFormat: CamFormat)
}
