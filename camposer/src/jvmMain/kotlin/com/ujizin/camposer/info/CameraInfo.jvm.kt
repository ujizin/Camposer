package com.ujizin.camposer.info

import com.ujizin.camposer.internal.core.jvm.JvmCameraController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

public actual class CameraInfo internal constructor(
  private val controller: JvmCameraController,
) {
  private val _state = MutableStateFlow(getCurrentState())
  public actual val state: StateFlow<CameraInfoState> = _state.asStateFlow()

  internal fun updateInfo() {
    _state.update { getCurrentState() }
  }

  private fun getCurrentState(): CameraInfoState {
    val capabilities = controller.capabilities
    val formats = listOfNotNull(controller.currentFormat)
    return CameraInfoState(
      isZoomSupported = capabilities.isZoomSupported,
      isExposureSupported = capabilities.isExposureSupported,
      minZoom = capabilities.minZoom,
      maxZoom = capabilities.maxZoom,
      minExposure = capabilities.minExposure,
      maxExposure = capabilities.maxExposure,
      isFlashSupported = capabilities.isFlashSupported,
      isFlashAvailable = capabilities.isFlashSupported,
      isTorchSupported = capabilities.isTorchSupported,
      isTorchAvailable = capabilities.isTorchSupported,
      isZeroShutterLagSupported = false,
      isVideoStabilizationSupported = capabilities.isVideoStabilizationSupported,
      isFocusSupported = capabilities.isFocusSupported,
      minFPS = capabilities.minFrameRate,
      maxFPS = capabilities.maxFrameRate,
      photoFormats = formats,
      videoFormats = formats,
    )
  }
}
