package com.ujizin.camposer.state.properties.format.config

/**
 * Configuration for the Aspect Ratio of the Camera.
 *
 * This class holds the desired aspect ratio (width / height) as a [Float].
 * It implements [CameraFormatConfig] to allow filtering camera streams based on their aspect ratio.
 * Common aspect ratios like 4:3 (approx 1.33) or 16:9 (approx 1.77) are often used.
 *
 * @property aspectRatio The target aspect ratio value (e.g., 4.0/3.0 for 4:3).
 *
 * @see CameraFormatConfig
 */
public class AspectRatioConfig(
  public val aspectRatio: Float,
) : CameraFormatConfig {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AspectRatioConfig) return false

    return aspectRatio == other.aspectRatio
  }

  override fun hashCode(): Int = aspectRatio.hashCode()

  override fun toString(): String {
    val displayAspectRatio = when (aspectRatio) {
      1F -> "1:1"
      in 1.3F..1.4F -> "4:3"
      in 1.7F..1.8F -> "16:9"
      else -> aspectRatio.toString()
    }

    return "AspectRatio(aspectRatio=$displayAspectRatio)"
  }
}
