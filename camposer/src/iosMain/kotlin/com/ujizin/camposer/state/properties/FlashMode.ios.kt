package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVCaptureFlashMode
import platform.AVFoundation.AVCaptureFlashModeAuto
import platform.AVFoundation.AVCaptureFlashModeOff
import platform.AVFoundation.AVCaptureFlashModeOn

internal val FlashMode.mode: AVCaptureFlashMode
  get() = when (this) {
    FlashMode.On -> AVCaptureFlashModeOn
    FlashMode.Auto -> AVCaptureFlashModeAuto
    FlashMode.Off -> AVCaptureFlashModeOff
  }

internal fun AVCaptureFlashMode.toFlashMode(): FlashMode =
  when (this) {
    AVCaptureFlashModeOn -> FlashMode.On
    AVCaptureFlashModeAuto -> FlashMode.Auto
    AVCaptureFlashModeOff -> FlashMode.Off
    else -> error("FlashMode not found")
  }
