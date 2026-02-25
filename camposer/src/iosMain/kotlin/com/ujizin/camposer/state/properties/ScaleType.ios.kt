package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVLayerVideoGravity
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill

internal val ScaleType.gravity: AVLayerVideoGravity
  get() = when (this) {
    ScaleType.FitStart,
    ScaleType.FitCenter,
    ScaleType.FitEnd,
    -> AVLayerVideoGravityResizeAspect

    ScaleType.FillStart,
    ScaleType.FillCenter,
    ScaleType.FillEnd,
    -> AVLayerVideoGravityResizeAspectFill
  }
