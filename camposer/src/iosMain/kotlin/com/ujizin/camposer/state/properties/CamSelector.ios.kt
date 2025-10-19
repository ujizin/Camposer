package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront

public actual enum class CamSelector(internal val position: AVCaptureDevicePosition) {
    Front(AVCaptureDevicePositionFront),
    Back(AVCaptureDevicePositionBack);
}
