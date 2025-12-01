package com.ujizin.camposer.state.properties.format.config

public class ResolutionConfig(
  public val width: Int,
  public val height: Int,
) : CameraFormatConfig {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ResolutionConfig) return false

    if (width != other.width) return false
    if (height != other.height) return false

    return true
  }

  override fun hashCode(): Int = 31 * width + height

  override fun toString(): String = "Resolution(width=$width, height=$height)"

  public companion object {
    public val UltraHigh: ResolutionConfig = ResolutionConfig(3840, 2160)
    public val High: ResolutionConfig = ResolutionConfig(1920, 1080)
    public val Medium: ResolutionConfig = ResolutionConfig(1280, 720)

    public val Low: ResolutionConfig = ResolutionConfig(720, 480)
  }
}
