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
public enum class ImageCaptureMode(@ImageCapture.CaptureMode internal val mode: Int) {
    @ExperimentalZeroShutterLag
    ZeroShutterLag(ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG),
    MaxQuality(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY),
    MinLatency(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY);
}
