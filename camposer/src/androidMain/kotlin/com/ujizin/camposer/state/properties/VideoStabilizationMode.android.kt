package com.ujizin.camposer.state.properties

internal fun VideoStabilizationMode.toAndroidStabilizationFlags(): Pair<Boolean, Boolean> {
  val video = this != VideoStabilizationMode.Off
  val preview = when (this) {
    VideoStabilizationMode.Cinematic,
    VideoStabilizationMode.CinematicExtended,
    VideoStabilizationMode.CinematicExtendedEnhanced,
    -> true

    else -> false
  }
  return video to preview
}
