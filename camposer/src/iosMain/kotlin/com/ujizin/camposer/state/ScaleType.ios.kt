package com.ujizin.camposer.state

import platform.AVFoundation.AVLayerVideoGravity
import platform.AVFoundation.AVLayerVideoGravityResize
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill

public actual enum class ScaleType(internal val gravity: AVLayerVideoGravity) {
    FitStart(AVLayerVideoGravityResize),
    FitCenter(AVLayerVideoGravityResize),
    FitEnd(AVLayerVideoGravityResize),
    FillStart(AVLayerVideoGravityResizeAspectFill),
    FillCenter(AVLayerVideoGravityResizeAspectFill),
    FillEnd(AVLayerVideoGravityResizeAspectFill),
}
