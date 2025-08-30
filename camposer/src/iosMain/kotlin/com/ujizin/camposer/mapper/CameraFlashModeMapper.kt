package com.ujizin.camposer.mapper

import com.ujizin.camposer.state.FlashMode
import platform.AVFoundation.AVCaptureFlashMode
import platform.AVFoundation.AVCaptureFlashModeAuto
import platform.AVFoundation.AVCaptureFlashModeOff
import platform.AVFoundation.AVCaptureFlashModeOn

public fun AVCaptureFlashMode.toFlashMode(): FlashMode = when (this) {
    AVCaptureFlashModeOn -> FlashMode.On
    AVCaptureFlashModeOff -> FlashMode.Off
    AVCaptureFlashModeAuto -> FlashMode.Auto
    else -> throw RuntimeException("Flash Mode not found in AVCaptureFlashMode")
}

public fun FlashMode.toAVCaptureFlashMode(): Long = when (this) {
    FlashMode.On -> AVCaptureFlashModeOn
    FlashMode.Auto -> AVCaptureFlashModeAuto
    FlashMode.Off -> AVCaptureFlashModeOff
}
