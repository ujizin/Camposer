package com.ujizin.camposer.internal.core

import com.ujizin.camposer.state.CameraState

internal interface CameraManagerInternal : CameraManagerDelegate {
  val cameraState: CameraState

  fun isMirrorEnabled(): Boolean
}
