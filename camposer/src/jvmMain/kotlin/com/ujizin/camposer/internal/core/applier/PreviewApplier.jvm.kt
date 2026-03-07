package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.MirrorMode
import com.ujizin.camposer.state.properties.ScaleType

internal actual class PreviewApplier(
  private val cameraState: CameraState,
) : CameraStateApplier {

  override fun onCameraInitialized() {
    cameraState.updateMirrorMode(MirrorMode.Off)
  }

  actual fun applyMirrorMode(mirrorMode: MirrorMode) {
    cameraState.updateMirrorMode(mirrorMode)
  }

  actual fun applyScaleType(scaleType: ScaleType) {
    cameraState.updateScaleType(scaleType)
  }

  actual fun applyFocusOnTapEnabled(isFocusOnTapEnabled: Boolean) {
    cameraState.updateFocusOnTapEnabled(isFocusOnTapEnabled)
  }
}
