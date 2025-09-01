package com.ujizin.camposer.extensions

import com.ujizin.camposer.utils.executeWithErrorHandling
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.position

internal val AVCaptureDevicePosition.captureDevice: AVCaptureDevice
    get() = platform.AVFoundation.AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
        listOf(platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera),
        platform.AVFoundation.AVMediaTypeVideo,
        platform.AVFoundation.AVCaptureDevicePositionUnspecified
    ).devices.firstOrNull {
        (it as? AVCaptureDevice)?.position == this
    } as AVCaptureDevice

@OptIn(ExperimentalForeignApi::class)
internal fun AVCaptureDevice.withConfigurationLock(block: AVCaptureDevice.() -> Unit) {
    executeWithErrorHandling { nsErrorPtr ->
        lockForConfiguration(nsErrorPtr)
        block()
        unlockForConfiguration()
    }
}