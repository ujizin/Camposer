package com.ujizin.camposer.info

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.internal.utils.CameraFormatUtils
import com.ujizin.camposer.state.properties.CameraData
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureOutput

public actual class CameraInfo internal constructor(
  private val controller: IOSCameraController,
) {
  public actual val isZoomSupported: Boolean by mutableStateOf(true)

  public actual var minZoom: Float by mutableFloatStateOf(1F)
    private set

  public actual var maxZoom: Float by mutableFloatStateOf(1F)
    private set

  public actual var isExposureSupported: Boolean by mutableStateOf(false)
    private set

  public actual var minExposure: Float by mutableFloatStateOf(0F)
    private set

  public actual var maxExposure: Float by mutableFloatStateOf(0F)
    private set

  public actual var isFlashSupported: Boolean by mutableStateOf(false)
    private set

  public actual var isFlashAvailable: Boolean by mutableStateOf(false)
    private set

  public actual var isTorchSupported: Boolean by mutableStateOf(false)
    private set

  public actual var isTorchAvailable: Boolean by mutableStateOf(false)
    private set

  public actual var isZeroShutterLagSupported: Boolean by mutableStateOf(false)
    private set

  public actual var isVideoStabilizationSupported: Boolean by mutableStateOf(false)
    private set

  public actual var isFocusSupported: Boolean by mutableStateOf(false)
    private set

  private val formats: List<AVCaptureDeviceFormat>
    get() = controller.captureDevice.formats.filterIsInstance<AVCaptureDeviceFormat>()

  public actual var photoFormats: List<CameraData> = listOf()
    get() = CameraFormatUtils.getPhotoFormats(formats)
    private set

  public actual var videoFormats: List<CameraData> = listOf()
    get() = CameraFormatUtils.getVideoFormats(formats)
    private set

  public actual var minFPS: Int by mutableIntStateOf(-1)
    internal set

  public actual var maxFPS: Int by mutableIntStateOf(-1)
    internal set

  internal val allFormats: List<CameraData>
    get() = (photoFormats + videoFormats).distinct()

  @OptIn(ExperimentalForeignApi::class)
  internal fun rebind(output: AVCaptureOutput) {
    val (minFps, maxFps) = controller.fpsRange
    val (minZoom, maxZoom) = controller.zoomRange
    val (minExposure, maxExposure) = controller.exposureCompensationRange
    this.minZoom = minZoom
    this.maxZoom = maxZoom
    this.minExposure = minExposure
    this.maxExposure = maxExposure
    this.minFPS = minFps
    this.maxFPS = maxFps
    this.isExposureSupported = controller.isExposureCompensationSupported
    isFocusSupported = controller.isFocusSupported
    isFlashSupported = controller.hasFlash
    isFlashAvailable = controller.isFlashAvailable
    isTorchSupported = controller.hasTorch
    isTorchAvailable = controller.isFlashAvailable
    isZeroShutterLagSupported = controller.isZeroShutterLagSupported(output)
    isVideoStabilizationSupported = isVideoStabilizationSupported()
  }
}
