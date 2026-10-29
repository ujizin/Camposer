package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.MirrorMode
import com.ujizin.camposer.state.properties.ScaleType

/**
 * The desktop preview reads scale type and mirror mode from state when it draws, so no
 * hardware write is needed here.
 */
internal actual class PreviewApplier(
  private val cameraState: CameraState,
) : CameraStateApplier {
  actual fun applyScaleType(scaleType: ScaleType) {
    cameraState.updateScaleType(scaleType)
  }

  actual fun applyFocusOnTapEnabled(isFocusOnTapEnabled: Boolean) {
    cameraState.updateFocusOnTapEnabled(isFocusOnTapEnabled)
  }

  actual fun applyMirrorMode(mirrorMode: MirrorMode) {
    cameraState.updateMirrorMode(mirrorMode)
  }
}
