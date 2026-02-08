package com.ujizin.camposer.info

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.state.properties.CameraData
import java.util.concurrent.Executor

public actual class CameraInfo internal constructor(
  private val mainExecutor: Executor,
  private val cameraInfo: AndroidCameraInfo,
) {
  public actual val isZoomSupported: Boolean by derivedStateOf {
    maxZoom != cameraInfo.initialZoom
  }

  public actual var minZoom: Float by mutableFloatStateOf(cameraInfo.minZoom)
    private set
  public actual var maxZoom: Float by mutableFloatStateOf(cameraInfo.maxZoom)
    private set

  public actual var isExposureSupported: Boolean by mutableStateOf(cameraInfo.isExposureSupported)
  public actual var minExposure: Float by mutableFloatStateOf(cameraInfo.minExposure)
    private set
  public actual var maxExposure: Float by mutableFloatStateOf(cameraInfo.maxExposure)
    private set
  public actual var isFlashSupported: Boolean by mutableStateOf(cameraInfo.isFlashSupported)
    internal set
  public actual var isFlashAvailable: Boolean by mutableStateOf(cameraInfo.isFlashSupported)
    internal set
  public actual var isTorchSupported: Boolean by mutableStateOf(cameraInfo.isFlashSupported)
    private set
  public actual var isTorchAvailable: Boolean by mutableStateOf(cameraInfo.isFlashSupported)
    private set
  public actual var isZeroShutterLagSupported: Boolean by mutableStateOf(
    cameraInfo.isZeroShutterLagSupported,
  )
    private set
  public actual var isFocusSupported: Boolean by mutableStateOf(cameraInfo.isFocusSupported)
    private set
  public actual var minFPS: Int by mutableIntStateOf(cameraInfo.minFPS)
    internal set
  public actual var maxFPS: Int by mutableIntStateOf(cameraInfo.maxFPS)
    internal set

  public actual var photoFormats: List<CameraData> = emptyList()
    get() = cameraInfo.photoFormats
    private set

  public actual var videoFormats: List<CameraData> = emptyList()
    get() = cameraInfo.videoFormats
    private set

  internal fun rebind() {
    minZoom = cameraInfo.minZoom
    maxZoom = cameraInfo.maxZoom
    minExposure = cameraInfo.minExposure
    maxExposure = cameraInfo.maxExposure
    isExposureSupported = cameraInfo.isExposureSupported
    isFlashSupported = cameraInfo.isFlashSupported
    isFlashAvailable = cameraInfo.isFlashSupported
    isTorchSupported = cameraInfo.isFlashSupported
    isTorchAvailable = cameraInfo.isFlashSupported
    isFocusSupported = cameraInfo.isFocusSupported
    isZeroShutterLagSupported = cameraInfo.isZeroShutterLagSupported
    minFPS = cameraInfo.minFPS
    maxFPS = cameraInfo.maxFPS
  }
}
