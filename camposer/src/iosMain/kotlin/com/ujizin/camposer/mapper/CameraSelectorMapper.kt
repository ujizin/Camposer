package com.ujizin.camposer.mapper

import com.ujizin.camposer.state.CamSelector
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.position

@OptIn(ExperimentalForeignApi::class)
internal fun CamSelector.toAVCaptureDevicePosition(): AVCaptureDevicePosition = when (this) {
    CamSelector.Front -> AVCaptureDevicePositionFront
    CamSelector.Back -> AVCaptureDevicePositionBack
}

internal fun AVCaptureDeviceInput?.toCamSelector(): CamSelector = when (this?.device?.position) {
    AVCaptureDevicePositionBack -> CamSelector.Back
    AVCaptureDevicePositionFront -> CamSelector.Front
    else -> throw RuntimeException("Cam selector not found in AVCaptureDeviceInput")
}