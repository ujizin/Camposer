package com.ujizin.camposer.state.properties.selector

import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront

public actual enum class CamPosition(internal val value: AVCaptureDevicePosition) {
    Back(AVCaptureDevicePositionBack),
    Front(AVCaptureDevicePositionFront),
}