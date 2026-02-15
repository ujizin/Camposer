package com.ujizin.camposer.shared.features.camera

public enum class AspectRatioOption(
  public val label: String,
  public val ratio: Float,
) {
  Ratio1x1(label = "1:1", ratio = 1F),
  Ratio4x3(label = "4:3", ratio = 4F / 3F),
  Ratio16x9(label = "16:9", ratio = 16F / 9F),
}
