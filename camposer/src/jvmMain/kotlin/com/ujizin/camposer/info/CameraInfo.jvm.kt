package com.ujizin.camposer.info

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

public actual class CameraInfo internal constructor(
  private val jvmCameraInfo: JvmCameraInfo,
) {
  private val _state = MutableStateFlow(getCurrentState())
  public actual val state: StateFlow<CameraInfoState> = _state.asStateFlow()

  internal fun rebind() {
    _state.update { getCurrentState() }
  }

  private fun getCurrentState(): CameraInfoState =
    CameraInfoState(
      isZoomSupported = jvmCameraInfo.isZoomSupported,
      isExposureSupported = jvmCameraInfo.isExposureSupported,
      minZoom = jvmCameraInfo.minZoom,
      maxZoom = jvmCameraInfo.maxZoom,
      minExposure = jvmCameraInfo.minExposure,
      maxExposure = jvmCameraInfo.maxExposure,
      isFlashSupported = jvmCameraInfo.isFlashSupported,
      isFlashAvailable = jvmCameraInfo.isFlashSupported,
      isTorchSupported = false,
      isTorchAvailable = false,
      isZeroShutterLagSupported = false,
      isVideoStabilizationSupported = false,
      isFocusSupported = false,
      minFPS = jvmCameraInfo.minFPS,
      maxFPS = jvmCameraInfo.maxFPS,
      photoFormats = emptyList(),
      videoFormats = emptyList(),
    )
}
