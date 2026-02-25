package com.ujizin.camposer.state.properties.selector

internal val CamLensType.minFov: Float
  get() = when (this) {
    CamLensType.Wide -> 61F
    CamLensType.UltraWide -> 94F
    CamLensType.Telephoto -> 0F
  }
