package com.ujizin.camposer.state.properties

import androidx.camera.view.CameraController.IMAGE_CAPTURE
import androidx.camera.view.CameraController.VIDEO_CAPTURE

internal val CaptureMode.value: Int
  get() = when (this) {
    CaptureMode.Image -> IMAGE_CAPTURE
    CaptureMode.Video -> VIDEO_CAPTURE
  }
