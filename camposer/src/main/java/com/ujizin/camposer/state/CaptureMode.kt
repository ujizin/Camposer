package com.ujizin.camposer.state

import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.camera.view.CameraController.IMAGE_CAPTURE
import androidx.camera.view.CameraController.VIDEO_CAPTURE
import androidx.camera.view.video.ExperimentalVideo

/**
 * Camera Capture mode.
 *
 * @param value internal camera capture from CameraX
 * @see IMAGE_CAPTURE
 * @see VIDEO_CAPTURE
 * */
@OptIn(markerClass = [ExperimentalVideo::class])
public enum class CaptureMode(internal val value: Int) {
    Image(IMAGE_CAPTURE),
    @RequiresApi(Build.VERSION_CODES.M)
    Video(VIDEO_CAPTURE),
}
