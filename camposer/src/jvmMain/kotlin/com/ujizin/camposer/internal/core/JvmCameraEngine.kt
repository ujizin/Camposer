package com.ujizin.camposer.internal.core

import com.ujizin.camposer.internal.core.jvm.JvmCameraController

internal interface JvmCameraEngine : CameraEngine {
  val jvmCameraController: JvmCameraController

  fun onCameraInitialized()
}
