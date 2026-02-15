package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDevicePositionFront

internal fun MirrorMode.isMirrorEnabled(position: AVCaptureDevicePosition): Boolean =
  when (this) {
    MirrorMode.On -> true
    MirrorMode.Off -> false
    MirrorMode.OnlyInFront -> position == AVCaptureDevicePositionFront
  }
