package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhotoOutput

/**
 * Camera Capture mode.
 * */
public actual enum class CaptureMode(
  internal val output: AVCaptureOutput,
) {
  Image(AVCapturePhotoOutput()),
  Video(AVCaptureMovieFileOutput()),
}
