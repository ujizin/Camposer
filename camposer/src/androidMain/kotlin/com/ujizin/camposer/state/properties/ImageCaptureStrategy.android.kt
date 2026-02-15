package com.ujizin.camposer.state.properties

import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture

@get:ImageCapture.CaptureMode
internal val ImageCaptureStrategy.mode: Int
  @OptIn(ExperimentalZeroShutterLag::class)
  get() = when (this) {
    ImageCaptureStrategy.MinLatency -> ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG
    ImageCaptureStrategy.MaxQuality -> ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
    ImageCaptureStrategy.Balanced -> ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
  }

@get:ImageCapture.CaptureMode
internal val ImageCaptureStrategy.fallback: Int
  get() = when (this) {
    ImageCaptureStrategy.MinLatency -> ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
    ImageCaptureStrategy.MaxQuality -> ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
    ImageCaptureStrategy.Balanced -> ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
  }
