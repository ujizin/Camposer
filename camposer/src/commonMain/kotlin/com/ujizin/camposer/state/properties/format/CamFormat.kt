package com.ujizin.camposer.state.properties.format

import androidx.compose.runtime.Stable
import com.ujizin.camposer.state.properties.format.config.CameraFormatConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig

/**
 * A class representing the camera format configuration.
 *
 * This class encapsulates a list of [CameraFormatConfig] that determines the
 * resolution and quality settings for the camera stream. The configurations are applied
 * based on priority order: the first configuration is attempted, followed by subsequent ones as fallbacks.
 *
 * You can use predefined formats via the companion object extensions:
 * - [CamFormat.Companion.UltraHigh]
 * - [CamFormat.Companion.High]
 * - [CamFormat.Companion.Medium]
 * - [CamFormat.Companion.Low]
 * - [CamFormat.Companion.Default]
 *
 * Or create a custom format by providing specific [CameraFormatConfig]s to the constructor.
 *
 * @see CameraFormatConfig
 * @see ResolutionConfig
 */
@Stable
public expect class CamFormat {
  public val configs: List<CameraFormatConfig>

  public constructor()

  public constructor(
    vararg configs: CameraFormatConfig,
  )

  override fun equals(other: Any?): Boolean

  override fun hashCode(): Int

  override fun toString(): String

  public companion object
}

public val CamFormat.Companion.UltraHigh: CamFormat
  get() = CamFormat(ResolutionConfig.UltraHigh)

public val CamFormat.Companion.High: CamFormat
  get() = CamFormat(ResolutionConfig.High)

public val CamFormat.Companion.Medium: CamFormat
  get() = CamFormat(ResolutionConfig.Medium)

public val CamFormat.Companion.Low: CamFormat
  get() = CamFormat(ResolutionConfig.Low)

public val CamFormat.Companion.Default: CamFormat
  get() = High
