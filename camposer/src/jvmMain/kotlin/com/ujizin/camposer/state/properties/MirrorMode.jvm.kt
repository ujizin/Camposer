package com.ujizin.camposer.state.properties

import com.ujizin.camposer.state.properties.selector.CamPosition

internal fun MirrorMode.isMirrorEnabled(camPosition: CamPosition): Boolean =
  when (this) {
    MirrorMode.On -> true
    MirrorMode.Off -> false
    MirrorMode.OnlyInFront -> camPosition == CamPosition.Front
  }
