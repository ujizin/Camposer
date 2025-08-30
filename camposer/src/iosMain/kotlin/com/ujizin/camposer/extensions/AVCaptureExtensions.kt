package com.ujizin.camposer.extensions

import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.position

internal val AVCaptureSession.captureDeviceInput: AVCaptureDeviceInput?
    get() = inputs.firstOrNull() as? AVCaptureDeviceInput

internal val AVCaptureDevicePosition.captureDevice: AVCaptureDevice?
    get() = platform.AVFoundation.AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
        listOf(platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera),
        platform.AVFoundation.AVMediaTypeVideo,
        platform.AVFoundation.AVCaptureDevicePositionUnspecified
    ).devices.firstOrNull {
        (it as? AVCaptureDevice)?.position == this
    } as? AVCaptureDevice
