package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDevicePositionFront

public actual enum class MirrorMode {
  On,
  OnlyInFront,
  Off,
  ;

  internal fun isMirrorEnabled(position: AVCaptureDevicePosition) =
    when (this) {
      On -> true
      Off -> false
      OnlyInFront -> position == AVCaptureDevicePositionFront
    }
}
