package com.ujizin.camposer.internal.core.jvm

/**
 * Hardware capabilities reported by a [JvmCameraController].
 *
 * Desktop webcams expose very little through the capture backend. Fields that no backend can
 * report (flash, torch, focus, stabilization, zoom) default to unsupported.
 */
internal data class JvmCameraCapabilities(
  val isExposureSupported: Boolean = false,
  val minExposure: Float = 0F,
  val maxExposure: Float = 0F,
  val isZoomSupported: Boolean = false,
  val minZoom: Float = 1F,
  val maxZoom: Float = 1F,
  val isFlashSupported: Boolean = false,
  val isTorchSupported: Boolean = false,
  val isVideoStabilizationSupported: Boolean = false,
  val isFocusSupported: Boolean = false,
  val minFrameRate: Int = 1,
  val maxFrameRate: Int = DEFAULT_MAX_FRAME_RATE,
) {
  internal companion object {
    const val DEFAULT_MAX_FRAME_RATE = 120
  }
}
