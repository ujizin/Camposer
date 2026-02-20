package com.ujizin.camposer

import com.ujizin.camposer.info.CameraInfoState
import com.ujizin.camposer.state.properties.CameraData

internal fun CameraInfoState.copy(
  isZoomSupported: Boolean = this.isZoomSupported,
  isExposureSupported: Boolean = this.isExposureSupported,
  minZoom: Float = this.minZoom,
  maxZoom: Float = this.maxZoom,
  minExposure: Float = this.minExposure,
  maxExposure: Float = this.maxExposure,
  isFlashSupported: Boolean = this.isFlashSupported,
  isFlashAvailable: Boolean = this.isFlashAvailable,
  isTorchSupported: Boolean = this.isTorchSupported,
  isTorchAvailable: Boolean = this.isTorchAvailable,
  isZeroShutterLagSupported: Boolean = this.isZeroShutterLagSupported,
  isVideoStabilizationSupported: Boolean = this.isVideoStabilizationSupported,
  isFocusSupported: Boolean = this.isFocusSupported,
  minFPS: Int = this.minFPS,
  maxFPS: Int = this.maxFPS,
  photoFormats: List<CameraData> = this.photoFormats,
  videoFormats: List<CameraData> = this.videoFormats,
): CameraInfoState =
  CameraInfoState(
    isZoomSupported = isZoomSupported,
    isExposureSupported = isExposureSupported,
    minZoom = minZoom,
    maxZoom = maxZoom,
    minExposure = minExposure,
    maxExposure = maxExposure,
    isFlashSupported = isFlashSupported,
    isFlashAvailable = isFlashAvailable,
    isTorchSupported = isTorchSupported,
    isTorchAvailable = isTorchAvailable,
    isZeroShutterLagSupported = isZeroShutterLagSupported,
    isVideoStabilizationSupported = isVideoStabilizationSupported,
    isFocusSupported = isFocusSupported,
    minFPS = minFPS,
    maxFPS = maxFPS,
    photoFormats = photoFormats,
    videoFormats = videoFormats,
  )
