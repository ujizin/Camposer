package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.state.properties.MirrorMode
import com.ujizin.camposer.state.properties.ScaleType

internal expect class PreviewApplier : CameraStateApplier {
  fun applyScaleType(scaleType: ScaleType)

  fun applyFocusOnTapEnabled(isFocusOnTapEnabled: Boolean)

  fun applyMirrorMode(mirrorMode: MirrorMode)
}
