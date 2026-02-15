package com.ujizin.camposer.state.properties

import androidx.camera.view.PreviewView.ScaleType as CameraScaleType

internal val ScaleType.type: CameraScaleType
  get() = when (this) {
    ScaleType.FitStart -> CameraScaleType.FIT_START
    ScaleType.FitCenter -> CameraScaleType.FIT_CENTER
    ScaleType.FitEnd -> CameraScaleType.FIT_END
    ScaleType.FillStart -> CameraScaleType.FILL_START
    ScaleType.FillCenter -> CameraScaleType.FILL_CENTER
    ScaleType.FillEnd -> CameraScaleType.FILL_END
  }
