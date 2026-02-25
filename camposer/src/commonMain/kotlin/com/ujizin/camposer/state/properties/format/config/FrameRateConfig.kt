package com.ujizin.camposer.state.properties.format.config

/**
 * Configuration class for setting the camera frame rate (FPS).
 *
 * This class is used to filter or configure the camera format based on a desired
 * frames per second (FPS) value.
 *
 * @param fps The desired frames per second.
 *
 * @see CameraFormatConfig
 */
public class FrameRateConfig(
  public val fps: Int,
) : CameraFormatConfig {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is FrameRateConfig) return false
    return fps == other.fps
  }

  override fun hashCode(): Int = 31 * fps

  override fun toString(): String = "FrameRate(fps=$fps)"
}
