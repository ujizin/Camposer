package com.ujizin.camposer.state.properties.selector

import platform.AVFoundation.AVCaptureDeviceType
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInLiDARDepthCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInUltraWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera

public actual enum class CamLensType(internal val type: AVCaptureDeviceType) {
    Wide(AVCaptureDeviceTypeBuiltInWideAngleCamera),
    UltraWide(AVCaptureDeviceTypeBuiltInUltraWideCamera),
    Telephoto(AVCaptureDeviceTypeBuiltInLiDARDepthCamera);
}