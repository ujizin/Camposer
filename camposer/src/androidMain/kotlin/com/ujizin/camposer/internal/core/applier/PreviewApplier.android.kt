package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.internal.core.camerax.CameraXController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.MirrorMode
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.mode

internal actual class PreviewApplier(
  private val cameraState: CameraState,
  private val cameraXController: CameraXController,
) : CameraStateApplier {
  override fun onCameraInitialized() {
    // Disable pinch-to-zoom in CameraX because Camposer uses a custom implementation
    cameraXController.isPinchToZoomEnabled = false

    applyFocusOnTapEnabled(cameraState.isFocusOnTapEnabled.value)
    applyMirrorMode(cameraState.mirrorMode.value)
  }

  actual fun applyScaleType(scaleType: ScaleType) {
    cameraState.updateScaleType(scaleType)
  }

  actual fun applyMirrorMode(mirrorMode: MirrorMode) {
    cameraXController.videoCaptureMirrorMode = mirrorMode.mode
    cameraState.updateMirrorMode(mirrorMode)
  }

  actual fun applyFocusOnTapEnabled(isFocusOnTapEnabled: Boolean) {
    cameraXController.isTapToFocusEnabled = isFocusOnTapEnabled
    cameraState.updateFocusOnTapEnabled(isFocusOnTapEnabled)
  }
}
