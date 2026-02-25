package com.ujizin.camposer.state.properties.format.config

import com.ujizin.camposer.state.properties.VideoStabilizationMode

/**
 * Configuration for video stabilization.
 *
 * This class holds the desired [VideoStabilizationMode] to be applied to the camera session.
 * It implements [CameraFormatConfig] to integrate with the camera configuration pipeline if supported.
 *
 * @param mode The video stabilization mode to use. Defaults to [VideoStabilizationMode.Standard].
 * @see VideoStabilizationMode
 *
 * @see CameraFormatConfig
 */
public class VideoStabilizationConfig(
  public val mode: VideoStabilizationMode = VideoStabilizationMode.Standard,
) : CameraFormatConfig {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is VideoStabilizationConfig) return false
    return mode == other.mode
  }

  override fun hashCode(): Int = mode.hashCode()

  override fun toString(): String = "VideoStabilizationConfig(mode=$mode)"
}
