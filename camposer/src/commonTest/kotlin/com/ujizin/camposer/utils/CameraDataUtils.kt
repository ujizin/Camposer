package com.ujizin.camposer.utils

import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.VideoStabilizationMode

object CameraDataUtils {
  val cameraDataHighResolution = CameraData(width = 1920, height = 1080, minFps = 8, maxFps = 60)

  val cameraDataMediumResolution = CameraData(width = 1280, height = 720, minFps = 8, maxFps = 30)
  val cameraDataLowResolution = CameraData(width = 640, height = 480, minFps = 8, maxFps = 24)
  val cameraDataDefault = cameraDataHighResolution

  val cameraDataVideoStabilizationHighCinematic = CameraData(
    width = 1920,
    height = 1080,
    minFps = 8,
    maxFps = 60,
    videoStabilizationModes = listOf(
      VideoStabilizationMode.CinematicExtendedEnhanced,
      VideoStabilizationMode.CinematicExtended,
      VideoStabilizationMode.Cinematic,
      VideoStabilizationMode.Standard,
    ),
  )

  val cameraDataVideoStabilizationHigh = CameraData(
    width = 1920,
    height = 1080,
    minFps = 8,
    maxFps = 60,
    videoStabilizationModes = listOf(VideoStabilizationMode.Standard),
  )

  val cameraDataHigh16X9_4KResolution =
    CameraData(width = 3840, height = 2160, minFps = 8, maxFps = 30)
  val cameraDataHigh4X3_4KResolution =
    CameraData(width = 4000, height = 3000, minFps = 8, maxFps = 30)

  val cameraDataStandardList = listOf(
    cameraDataHighResolution,
    cameraDataMediumResolution,
    cameraDataLowResolution,
  )

  val cameraDataStabilizationList = buildList {
    add(cameraDataVideoStabilizationHighCinematic)
    add(cameraDataVideoStabilizationHigh)
    addAll(cameraDataStandardList)
  }
}
