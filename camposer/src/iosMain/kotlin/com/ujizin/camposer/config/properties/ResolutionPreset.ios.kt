package com.ujizin.camposer.config.properties

import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPreset
import platform.AVFoundation.AVCaptureSessionPreset1920x1080
import platform.AVFoundation.AVCaptureSessionPreset3840x2160
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureSessionPresetLow
import platform.AVFoundation.AVCaptureSessionPresetMedium

public actual enum class ResolutionPreset(internal vararg val presets: AVCaptureSessionPreset) {
    Default(AVCaptureSessionPresetHigh),
    UltraHigh(AVCaptureSessionPreset3840x2160),
    High(AVCaptureSessionPreset1920x1080),
    Medium(AVCaptureSessionPresetMedium),
    Low(AVCaptureSessionPresetLow);

    internal fun getOrClosestPreset(captureSession: AVCaptureSession): AVCaptureSessionPreset {
        val preset = presets.firstOrNull(captureSession::canSetSessionPreset)

        if (preset != null) return preset

        val next = entries.getOrNull(ordinal + 1)
        if (next == null) return Default.presets.first()

        return next.getOrClosestPreset(captureSession)
    }
}
