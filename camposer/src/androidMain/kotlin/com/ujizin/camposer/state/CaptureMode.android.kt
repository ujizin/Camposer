
package com.ujizin.camposer.state

import androidx.camera.view.CameraController.IMAGE_CAPTURE
import androidx.camera.view.CameraController.VIDEO_CAPTURE

/**
 * Camera Capture mode.
 *
 * @param value internal camera capture from CameraX
 * @see IMAGE_CAPTURE
 * @see VIDEO_CAPTURE
 * */
public actual enum class CaptureMode(internal val value: Int) {
    Image(IMAGE_CAPTURE),

    Video(VIDEO_CAPTURE),
}
