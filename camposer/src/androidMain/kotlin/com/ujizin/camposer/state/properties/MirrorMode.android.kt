package com.ujizin.camposer.state.properties

import androidx.camera.core.MirrorMode.MIRROR_MODE_OFF
import androidx.camera.core.MirrorMode.MIRROR_MODE_ON
import androidx.camera.core.MirrorMode.MIRROR_MODE_ON_FRONT_ONLY

public actual enum class MirrorMode(
  internal val mode: Int,
) {
  On(MIRROR_MODE_ON),
  OnlyInFront(MIRROR_MODE_ON_FRONT_ONLY),
  Off(MIRROR_MODE_OFF),
}
