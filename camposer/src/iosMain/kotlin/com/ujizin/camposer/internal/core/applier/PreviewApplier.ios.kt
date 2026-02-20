package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.gravity

internal class PreviewApplier(
  private val cameraState: CameraState,
  private val iOSCameraController: IOSCameraController,
) : CameraStateApplier {
  override fun onCameraInitialized() {
    applyScaleType(cameraState.scaleType.value)
  }

  fun applyScaleType(scaleType: ScaleType) {
    iOSCameraController.setPreviewGravity(scaleType.gravity)
    cameraState.updateScaleType(scaleType)
  }
}
