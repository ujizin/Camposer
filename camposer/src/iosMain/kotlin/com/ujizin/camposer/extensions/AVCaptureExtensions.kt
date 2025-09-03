package com.ujizin.camposer.extensions

import com.ujizin.camposer.utils.executeWithErrorHandling
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTripleCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.position

internal val AVCaptureDevicePosition.captureDevice: AVCaptureDevice
    get() = platform.AVFoundation.AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
        listOf( // TODO check how to make an API of this
            AVCaptureDeviceTypeBuiltInDualCamera,       // dual lens (wide + tele)
            AVCaptureDeviceTypeBuiltInTripleCamera,     // triple lens (wide + tele + ultra wide)
            AVCaptureDeviceTypeBuiltInDualWideCamera,   // wide + ultra wide
            AVCaptureDeviceTypeBuiltInWideAngleCamera,
        ),
        platform.AVFoundation.AVMediaTypeVideo,
        platform.AVFoundation.AVCaptureDevicePositionUnspecified
    ).devices.firstOrNull {
        (it as? AVCaptureDevice)?.position == this
    } as AVCaptureDevice
