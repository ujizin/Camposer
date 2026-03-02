package com.ujizin.camposer.info

import com.ujizin.camposer.internal.capture.FakeJvmCameraCapture

/**
 * Test implementation of [JvmCameraInfo] that returns configurable capability flags.
 *
 * Defaults to "all supported" so that tests which do not explicitly opt out of a feature
 * work the same way as on iOS (where [FakeIosCameraController] also defaults to supported=true).
 */
internal class FakeJvmCameraInfo : JvmCameraInfo(FakeJvmCameraCapture()) {
  override var isFlashSupported: Boolean = true
  override var isExposureSupported: Boolean = true
  override val isZoomSupported: Boolean = true
  override val minZoom: Float = 1f
  override val maxZoom: Float = 10f
  override val minExposure: Float = -5f
  override val maxExposure: Float = 5f
  override val minFPS: Int = 0
  override val maxFPS: Int = 30
}
