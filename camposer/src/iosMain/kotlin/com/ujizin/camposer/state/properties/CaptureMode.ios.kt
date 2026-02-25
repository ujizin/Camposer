package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhotoOutput

private val photoOutput: AVCaptureOutput = AVCapturePhotoOutput()
private val videoOutput: AVCaptureOutput = AVCaptureMovieFileOutput()

internal val CaptureMode.output: AVCaptureOutput
  get() = when (this) {
    CaptureMode.Image -> photoOutput
    CaptureMode.Video -> videoOutput
  }
