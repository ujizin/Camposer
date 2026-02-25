package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVCapturePhotoQualityPrioritization
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationBalanced
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationQuality
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationSpeed

internal val ImageCaptureStrategy.quality: AVCapturePhotoQualityPrioritization
  get() = when (this) {
    ImageCaptureStrategy.MinLatency -> AVCapturePhotoQualityPrioritizationSpeed
    ImageCaptureStrategy.MaxQuality -> AVCapturePhotoQualityPrioritizationQuality
    ImageCaptureStrategy.Balanced -> AVCapturePhotoQualityPrioritizationBalanced
  }

internal val ImageCaptureStrategy.highResolutionEnabled: Boolean
  get() = when (this) {
    ImageCaptureStrategy.MinLatency -> false
    ImageCaptureStrategy.MaxQuality -> true
    ImageCaptureStrategy.Balanced -> false
  }
