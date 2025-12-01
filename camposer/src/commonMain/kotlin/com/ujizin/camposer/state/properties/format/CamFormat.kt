package com.ujizin.camposer.state.properties.format

import com.ujizin.camposer.state.properties.format.config.CameraFormatConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig

public expect class CamFormat {
  public val configs: List<CameraFormatConfig>

  public constructor()

  public constructor(
    vararg configs: CameraFormatConfig,
  )

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
