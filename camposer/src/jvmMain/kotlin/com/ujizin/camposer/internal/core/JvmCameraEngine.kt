package com.ujizin.camposer.internal.core

import com.ujizin.camposer.internal.capture.JvmCameraCapture

internal interface JvmCameraEngine : CameraEngine {
  val capture: JvmCameraCapture
}
