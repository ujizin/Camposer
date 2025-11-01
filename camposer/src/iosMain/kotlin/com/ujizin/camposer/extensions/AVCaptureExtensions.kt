package com.ujizin.camposer.extensions

import com.ujizin.camposer.utils.executeWithErrorHandling
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTripleCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureFlashMode
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeLeft
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoOrientationPortraitUpsideDown
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.position
import platform.UIKit.UIInterfaceOrientation
import platform.UIKit.UIInterfaceOrientationLandscapeLeft
import platform.UIKit.UIInterfaceOrientationLandscapeRight
import platform.UIKit.UIInterfaceOrientationPortraitUpsideDown

internal val AVCaptureDevicePosition.captureDevice: AVCaptureDevice
    get() = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
        listOf(
            AVCaptureDeviceTypeBuiltInTripleCamera,
            AVCaptureDeviceTypeBuiltInDualCamera,
            AVCaptureDeviceTypeBuiltInDualWideCamera,
            AVCaptureDeviceTypeBuiltInWideAngleCamera,
        ),
        AVMediaTypeVideo,
        platform.AVFoundation.AVCaptureDevicePositionUnspecified
    ).devices.firstOrNull { device ->
        (device as? AVCaptureDevice)?.position == this
    } as AVCaptureDevice

internal fun AVCaptureSession.tryAddInput(
    input: AVCaptureDeviceInput,
): Boolean {
    if (inputs.contains(input)) return true

    return canAddInput(input).apply {
        if (this) addInput(input)
    }
}


internal fun AVCaptureSession.tryAddOutput(
    output: AVCaptureOutput,
): Boolean {
    if (outputs.contains(output)) return true

    return canAddOutput(output).apply {
        if (this) addOutput(output)
    }
}

internal fun AVCaptureSession.isFlashModeSupported(flashMode: AVCaptureFlashMode): Boolean {
    val output = outputs.firstIsInstanceOrNull<AVCapturePhotoOutput>()
    return output?.supportedFlashModes?.contains(flashMode) ?: false
}

internal fun AVCaptureOutput.setMirrorEnabled(isMirrored: Boolean) {
    connectionWithMediaType(AVMediaTypeVideo)?.setVideoMirrored(isMirrored)
}

@OptIn(ExperimentalForeignApi::class)
internal fun AVCaptureDevice.toDeviceInput(): AVCaptureDeviceInput =
    executeWithErrorHandling { ptr ->
        AVCaptureDeviceInput.deviceInputWithDevice(
            this,
            ptr
        )!!
    }

@OptIn(ExperimentalForeignApi::class)
internal fun AVCaptureDevice.withConfigurationLock(
    block: AVCaptureDevice.() -> Unit,
) = executeWithErrorHandling { nsErrorPtr ->
    try {
        lockForConfiguration(nsErrorPtr)
        block()
    } finally {
        unlockForConfiguration()
    }
}

internal fun UIInterfaceOrientation.toVideoOrientation(): AVCaptureVideoOrientation = when (this) {
    UIInterfaceOrientationLandscapeLeft -> AVCaptureVideoOrientationLandscapeLeft
    UIInterfaceOrientationLandscapeRight -> AVCaptureVideoOrientationLandscapeRight
    UIInterfaceOrientationPortraitUpsideDown -> AVCaptureVideoOrientationPortraitUpsideDown
    else -> AVCaptureVideoOrientationPortrait
}
