package com.ujizin.camposer.config.properties

import platform.AVFoundation.AVCapturePhotoQualityPrioritization
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationBalanced
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationQuality
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationSpeed

public actual enum class ImageCaptureStrategy(
    internal val strategy: AVCapturePhotoQualityPrioritization,
    internal val highResolutionEnabled: Boolean,
) {
    MinLatency(AVCapturePhotoQualityPrioritizationSpeed, false),
    MaxQuality(AVCapturePhotoQualityPrioritizationQuality, true),
    Balanced(AVCapturePhotoQualityPrioritizationBalanced, false),
}