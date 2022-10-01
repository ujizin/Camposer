package br.com.devlucasyuji.camposer.state

import androidx.annotation.OptIn
import androidx.camera.view.CameraController.IMAGE_CAPTURE
import androidx.camera.view.CameraController.VIDEO_CAPTURE
import androidx.camera.view.video.ExperimentalVideo

@OptIn(markerClass = [ExperimentalVideo::class])
enum class CaptureMode(internal val value: Int) {
    Image(IMAGE_CAPTURE),
    Video(VIDEO_CAPTURE),
}