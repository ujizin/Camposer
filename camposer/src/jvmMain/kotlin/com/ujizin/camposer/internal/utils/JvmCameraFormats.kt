package com.ujizin.camposer.internal.utils

import com.ujizin.camposer.state.properties.CameraData

private const val MIN_FRAME_RATE = 1
private const val PREFERRED_MAX_FRAME_RATE = 60

/**
 * Desktop capture backends cannot enumerate the formats a webcam supports. The format picker
 * scores these common candidates instead; the backend then snaps the request to the nearest
 * resolution it can deliver.
 */
internal object JvmCameraFormats {
  // A table of standard resolutions; naming each number would not make it clearer.
  @Suppress("MagicNumber")
  private val resolutions = listOf(
    3840 to 2160,
    2560 to 1440,
    1920 to 1080,
    1600 to 900,
    1280 to 720,
    1024 to 576,
    960 to 540,
    800 to 600,
    640 to 480,
    640 to 360,
    320 to 240,
  )

  fun candidates(currentFrameRate: Int): List<CameraData> {
    val maxFrameRate = maxOf(currentFrameRate, PREFERRED_MAX_FRAME_RATE)
    return resolutions.map { (width, height) ->
      CameraData(
        width = width,
        height = height,
        isFocusSupported = false,
        minFps = MIN_FRAME_RATE,
        maxFps = maxFrameRate,
      )
    }
  }
}
