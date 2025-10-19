package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVCaptureFlashMode
import platform.AVFoundation.AVCaptureFlashModeAuto
import platform.AVFoundation.AVCaptureFlashModeOff
import platform.AVFoundation.AVCaptureFlashModeOn

public actual enum class FlashMode(internal val mode: AVCaptureFlashMode) {
    On(AVCaptureFlashModeOn),
    Auto(AVCaptureFlashModeAuto),
    Off(AVCaptureFlashModeOff);

    internal companion object {
        fun AVCaptureFlashMode.toFlashMode(): FlashMode = entries.find {
            it.mode == this
        } ?: throw IllegalStateException("FlashMode not found")
    }
}