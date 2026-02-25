package com.ujizin.camposer.state.properties

import androidx.camera.core.MirrorMode.MIRROR_MODE_OFF
import androidx.camera.core.MirrorMode.MIRROR_MODE_ON
import androidx.camera.core.MirrorMode.MIRROR_MODE_ON_FRONT_ONLY

internal val MirrorMode.mode: Int
  get() = when (this) {
    MirrorMode.On -> MIRROR_MODE_ON
    MirrorMode.OnlyInFront -> MIRROR_MODE_ON_FRONT_ONLY
    MirrorMode.Off -> MIRROR_MODE_OFF
  }
