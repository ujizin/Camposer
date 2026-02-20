package com.ujizin.camposer.info

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.NONE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

public actual class CameraInfo internal constructor(
  private val cameraInfo: AndroidCameraInfo,
) {
  private val _state = MutableStateFlow(getCurrentState())
  public actual val state: StateFlow<CameraInfoState> = _state.asStateFlow()

  internal fun rebind() {
    _state.update { getCurrentState() }
  }

  private fun getCurrentState(): CameraInfoState {
    val isFlashSupported = cameraInfo.isFlashSupported
    val photoFormats = cameraInfo.photoFormats
    val videoFormats = cameraInfo.videoFormats
    return CameraInfoState(
      isZoomSupported = cameraInfo.maxZoom != cameraInfo.initialZoom,
      isExposureSupported = cameraInfo.isExposureSupported,
      minZoom = cameraInfo.minZoom,
      maxZoom = cameraInfo.maxZoom,
      minExposure = cameraInfo.minExposure,
      maxExposure = cameraInfo.maxExposure,
      isFlashSupported = isFlashSupported,
      isFlashAvailable = isFlashSupported,
      isTorchSupported = isFlashSupported,
      isTorchAvailable = isFlashSupported,
      isZeroShutterLagSupported = cameraInfo.isZeroShutterLagSupported,
      isVideoStabilizationSupported = videoFormats.isVideoStabilizationSupported(),
      isFocusSupported = cameraInfo.isFocusSupported,
      minFPS = cameraInfo.minFPS,
      maxFPS = cameraInfo.maxFPS,
      photoFormats = photoFormats,
      videoFormats = videoFormats,
    )
  }

  @VisibleForTesting(NONE)
  internal fun updateStateForTesting(updater: (CameraInfoState) -> CameraInfoState) {
    _state.update(updater)
  }
}
