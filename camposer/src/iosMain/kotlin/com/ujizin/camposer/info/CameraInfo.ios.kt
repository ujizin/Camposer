package com.ujizin.camposer.info

import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.internal.utils.CameraFormatUtils
import com.ujizin.camposer.state.properties.CameraData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureOutput

public actual class CameraInfo internal constructor(
  private val controller: IOSCameraController,
) {
  private val _state = MutableStateFlow(CameraInfoState())
  public actual val state: StateFlow<CameraInfoState> = _state.asStateFlow()

  private val formats: List<AVCaptureDeviceFormat>
    get() = controller.captureDevice.formats.filterIsInstance<AVCaptureDeviceFormat>()

  internal val allFormats: List<CameraData>
    get() = (state.value.photoFormats + state.value.videoFormats).distinct()

  @OptIn(ExperimentalForeignApi::class)
  internal fun rebind(output: AVCaptureOutput) {
    val (minFps, maxFps) = controller.fpsRange
    val (minZoom, maxZoom) = controller.zoomRange
    val (minExposure, maxExposure) = controller.exposureCompensationRange
    val photoFormats = CameraFormatUtils.getPhotoFormats(formats)
    val videoFormats = CameraFormatUtils.getVideoFormats(formats)
    _state.update {
      CameraInfoState(
        isZoomSupported = true,
        isExposureSupported = controller.isExposureCompensationSupported,
        minZoom = minZoom,
        maxZoom = maxZoom,
        minExposure = minExposure,
        maxExposure = maxExposure,
        isFlashSupported = controller.hasFlash,
        isFlashAvailable = controller.isFlashAvailable,
        isTorchSupported = controller.hasTorch,
        isTorchAvailable = controller.isFlashAvailable,
        isZeroShutterLagSupported = controller.isZeroShutterLagSupported(output),
        isVideoStabilizationSupported = videoFormats.isVideoStabilizationSupported(),
        isFocusSupported = controller.isFocusSupported,
        minFPS = minFps,
        maxFPS = maxFps,
        photoFormats = photoFormats,
        videoFormats = videoFormats,
      )
    }
  }
}
