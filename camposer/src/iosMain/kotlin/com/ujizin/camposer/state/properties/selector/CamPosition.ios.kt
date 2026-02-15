package com.ujizin.camposer.state.properties.selector

import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDevicePositionUnspecified

internal val CamPosition.value: AVCaptureDevicePosition
  get() = when (this) {
    CamPosition.Back -> AVCaptureDevicePositionBack
    CamPosition.Front -> AVCaptureDevicePositionFront
    CamPosition.External,
    CamPosition.Unknown,
    -> AVCaptureDevicePositionUnspecified
  }
