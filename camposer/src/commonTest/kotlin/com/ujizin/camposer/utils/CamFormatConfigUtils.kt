package com.ujizin.camposer.utils

import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.format.config.FrameRateConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig

object CamFormatConfigUtils {

  val defaultConfigs = listOf(
    ResolutionConfig.High,
    FrameRateConfig(30),
  )

  fun convertResolutionConfig(cameraData: CameraData) = ResolutionConfig(
    width = cameraData.width,
    height = cameraData.height,
  )
}