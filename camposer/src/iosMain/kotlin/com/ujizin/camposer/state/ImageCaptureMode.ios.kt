package com.ujizin.camposer.state

import platform.AVFoundation.AVCapturePhotoQualityPrioritization
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationBalanced
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationQuality
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationSpeed

public actual enum class ImageCaptureMode(
    internal val strategy: AVCapturePhotoQualityPrioritization,
    internal val highResolutionEnabled: Boolean,
) {
    MinLatency(AVCapturePhotoQualityPrioritizationSpeed, false),
    MaxQuality(AVCapturePhotoQualityPrioritizationQuality, true),
    Balanced(AVCapturePhotoQualityPrioritizationBalanced, false),
}