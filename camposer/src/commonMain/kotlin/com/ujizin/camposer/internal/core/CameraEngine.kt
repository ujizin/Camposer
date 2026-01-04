package com.ujizin.camposer.internal.core

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.CameraState

internal interface CameraEngine : CameraEngineDelegate {
  val cameraController: CameraController

  val cameraState: CameraState

  val cameraInfo: CameraInfo

  fun isMirrorEnabled(): Boolean
}
