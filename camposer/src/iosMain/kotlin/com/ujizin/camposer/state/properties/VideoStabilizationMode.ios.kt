package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVCaptureVideoStabilizationMode
import platform.AVFoundation.AVCaptureVideoStabilizationModeCinematic
import platform.AVFoundation.AVCaptureVideoStabilizationModeCinematicExtended
import platform.AVFoundation.AVCaptureVideoStabilizationModeCinematicExtendedEnhanced
import platform.AVFoundation.AVCaptureVideoStabilizationModeOff
import platform.AVFoundation.AVCaptureVideoStabilizationModeStandard

internal val VideoStabilizationMode.value: AVCaptureVideoStabilizationMode
  get() = when (this) {
    VideoStabilizationMode.Off -> {
      AVCaptureVideoStabilizationModeOff
    }

    VideoStabilizationMode.Standard -> {
      AVCaptureVideoStabilizationModeStandard
    }

    VideoStabilizationMode.Cinematic -> {
      AVCaptureVideoStabilizationModeCinematic
    }

    VideoStabilizationMode.CinematicExtended -> {
      AVCaptureVideoStabilizationModeCinematicExtended
    }

    VideoStabilizationMode.CinematicExtendedEnhanced -> {
      AVCaptureVideoStabilizationModeCinematicExtendedEnhanced
    }
  }
