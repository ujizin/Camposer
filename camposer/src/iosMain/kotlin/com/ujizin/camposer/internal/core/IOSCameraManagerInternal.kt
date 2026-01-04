package com.ujizin.camposer.internal.core

import com.ujizin.camposer.internal.core.ios.IOSCameraController
import platform.AVFoundation.AVCaptureVideoOrientation

internal interface IOSCameraManagerInternal : CameraManagerInternal {
  val cameraController: IOSCameraController

  fun getCurrentVideoOrientation(): AVCaptureVideoOrientation
}
