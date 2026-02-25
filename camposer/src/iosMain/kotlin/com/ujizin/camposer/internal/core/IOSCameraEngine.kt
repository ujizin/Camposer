package com.ujizin.camposer.internal.core

import com.ujizin.camposer.internal.core.ios.IOSCameraController
import platform.AVFoundation.AVCaptureVideoOrientation

internal interface IOSCameraEngine : CameraEngine {
  val iOSCameraController: IOSCameraController

  fun getCurrentVideoOrientation(): AVCaptureVideoOrientation
}
