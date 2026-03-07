package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.MirrorMode
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.gravity

internal actual class PreviewApplier(
  private val cameraState: CameraState,
  private val iOSCameraController: IOSCameraController,
) : CameraStateApplier {
  override fun onCameraInitialized() {
    applyScaleType(cameraState.scaleType.value)
  }

  actual fun applyScaleType(scaleType: ScaleType) {
    iOSCameraController.setPreviewGravity(scaleType.gravity)
    cameraState.updateScaleType(scaleType)
  }

  actual fun applyFocusOnTapEnabled(isFocusOnTapEnabled: Boolean) {
    cameraState.updateFocusOnTapEnabled(isFocusOnTapEnabled)
  }

  actual fun applyMirrorMode(mirrorMode: MirrorMode) {
    cameraState.updateMirrorMode(mirrorMode)
  }
}
