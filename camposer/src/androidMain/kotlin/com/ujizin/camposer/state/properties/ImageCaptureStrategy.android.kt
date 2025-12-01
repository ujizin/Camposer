package com.ujizin.camposer.state.properties

import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture

/**
 * Camera Image Capture Strategy.
 *
 * @param mode internal camera image capture mode from CameraX
 * @property MinLatency uses [ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG], if not supported use [ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY]
 * @property MaxQuality uses [ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY]
 * @property Balanced uses [ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY]
 * */
public actual enum class ImageCaptureStrategy(
  @get:ImageCapture.CaptureMode internal val mode: Int,
  internal val fallback: Int = mode,
) {
  @ExperimentalZeroShutterLag
  MinLatency(
    ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG,
    ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY,
  ),
  MaxQuality(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY),
  Balanced(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY),
}
