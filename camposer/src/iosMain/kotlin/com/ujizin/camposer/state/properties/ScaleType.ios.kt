package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVLayerVideoGravity
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill

public actual enum class ScaleType(internal val gravity: AVLayerVideoGravity) {
    FitStart(AVLayerVideoGravityResizeAspect),
    FitCenter(AVLayerVideoGravityResizeAspect),
    FitEnd(AVLayerVideoGravityResizeAspect),
    FillStart(AVLayerVideoGravityResizeAspectFill),
    FillCenter(AVLayerVideoGravityResizeAspectFill),
    FillEnd(AVLayerVideoGravityResizeAspectFill),
}
