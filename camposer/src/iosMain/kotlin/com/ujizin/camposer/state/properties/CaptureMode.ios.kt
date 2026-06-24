package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhotoOutput

internal fun CaptureMode.createOutput(): AVCaptureOutput =
  when (this) {
    CaptureMode.Image -> AVCapturePhotoOutput()
    CaptureMode.Video -> AVCaptureMovieFileOutput()
  }
