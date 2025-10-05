package com.ujizin.camposer.state

import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture

/**
 * Camera Image Capture mode.
 *
 * @param mode internal camera image capture mode from CameraX
 * @see ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG
 * @see ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
 * @see ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
 * */
public actual enum class ImageCaptureStrategy(
    @ImageCapture.CaptureMode internal val mode: Int,
    internal val fallback: Int = mode,
) {
    @ExperimentalZeroShutterLag
    MinLatency(
        ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG,
        ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
    ),
    MaxQuality(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY),
    Balanced(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY);
}
