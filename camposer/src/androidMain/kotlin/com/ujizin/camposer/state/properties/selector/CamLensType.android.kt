package com.ujizin.camposer.state.properties.selector

public actual enum class CamLensType(
  internal val minFov: Float,
) {
  Wide(61F),
  UltraWide(94F),
  Telephoto(0F),
}
