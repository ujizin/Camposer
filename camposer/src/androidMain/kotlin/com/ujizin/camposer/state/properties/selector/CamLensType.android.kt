package com.ujizin.camposer.state.properties.selector

private const val WIDE_MIN_FOV = 61F
private const val ULTRA_WIDE_MIN_FOV = 94F

internal val CamLensType.minFov: Float
  get() = when (this) {
    CamLensType.Wide -> WIDE_MIN_FOV
    CamLensType.UltraWide -> ULTRA_WIDE_MIN_FOV
    CamLensType.Telephoto -> 0F
  }
