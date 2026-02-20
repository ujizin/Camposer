package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.highResolutionEnabled
import com.ujizin.camposer.state.properties.mode
import com.ujizin.camposer.state.properties.output
import com.ujizin.camposer.state.properties.quality
import com.ujizin.camposer.state.properties.selector.CamSelector
import com.ujizin.camposer.state.properties.selector.getCaptureDevice
import com.ujizin.camposer.state.properties.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import platform.AVFoundation.AVCaptureOutput

internal class SessionTopologyApplier(
  private val cameraState: CameraState,
  private val cameraInfo: CameraInfo,
  private val iOSCameraController: IOSCameraController,
) : CameraStateApplier {
  private val sessionTopologyMutex = Mutex()

  private var captureModeJob: Job? = null
  private var camSelectorJob: Job? = null

  fun applyCaptureMode(captureMode: CaptureMode) {
    captureModeJob?.cancel()
    captureModeJob = lockedLaunch {
      if (cameraState.captureMode.value == captureMode) return@lockedLaunch
      applyCaptureModeInternal(
        previousCaptureMode = cameraState.captureMode.value,
        captureMode = captureMode,
      )
    }
  }

  fun applyCamSelector(camSelector: CamSelector) {
    camSelectorJob?.cancel()
    camSelectorJob = lockedLaunch {
      if (cameraState.camSelector.value == camSelector) return@lockedLaunch
      applyCamSelectorInternal(camSelector)
    }
  }

  private fun applyCaptureModeInternal(
    previousCaptureMode: CaptureMode,
    captureMode: CaptureMode,
  ) {
    resetConfig(captureMode.output)
    removeCaptureMode(previousCaptureMode)
    iOSCameraController.addOutput(captureMode.output)
    updateConfig(captureMode = captureMode, captureModeChanged = true)
    cameraState.updateCaptureMode(captureMode)
  }

  private fun applyCamSelectorInternal(camSelector: CamSelector) {
    resetConfig(cameraState.captureMode.value.output)
    iOSCameraController.setCaptureDevice(iOSCameraController.getCaptureDevice(camSelector))
    updateConfig(
      captureMode = cameraState.captureMode.value,
      camSelectorChanged = true,
    )
    cameraState.updateCamSelector(camSelector)
  }

  fun applyCamFormat(camFormat: CamFormat) {
    setCamFormat(
      camFormat = camFormat,
      captureMode = cameraState.captureMode.value,
    )
    cameraState.updateCamFormat(camFormat)
  }

  private fun removeCaptureMode(captureMode: CaptureMode) {
    iOSCameraController.removeOutput(captureMode.output)
  }

  private fun setCamFormat(
    camFormat: CamFormat,
    captureMode: CaptureMode,
  ) {
    camFormat.applyConfigs(
      cameraInfo = cameraInfo,
      iosCameraController = iOSCameraController,
      onDeviceFormatUpdated = { cameraInfo.rebind(output = captureMode.output) },
      onStabilizationModeChanged = {
        setVideoStabilizationMode(it)
        cameraState.updateVideoStabilizationMode(it)
      },
      onFrameRateChanged = {
        setFrameRate(it)
        cameraState.updateFrameRate(it)
      },
    )
  }

  private fun setFrameRate(frameRate: Int) {
    iOSCameraController.setFrameRate(frameRate)
  }

  private fun setVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode) {
    iOSCameraController.setVideoStabilization(videoStabilizationMode.value)
  }

  private fun setImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy) {
    iOSCameraController.setCameraOutputQuality(
      quality = imageCaptureStrategy.quality,
      highResolutionEnabled = imageCaptureStrategy.highResolutionEnabled,
    )
  }

  private fun setZoomRatio(zoomRatio: Float) {
    iOSCameraController.setZoomRatio(zoomRatio)
  }

  private fun setExposureCompensation(exposureCompensation: Float) {
    iOSCameraController.setExposureCompensation(exposureCompensation)
  }

  private fun setFlashMode(flashMode: FlashMode) {
    iOSCameraController.setFlashMode(flashMode.mode)
  }

  private fun setTorchEnabled(isTorchEnabled: Boolean) {
    iOSCameraController.setTorchEnabled(isTorchEnabled)
  }

  private fun resetConfig(captureOutput: AVCaptureOutput) {
    cameraInfo.rebind(output = captureOutput)

    setZoomRatio(cameraInfo.minZoom)
    cameraState.updateZoomRatio(cameraInfo.minZoom)

    setExposureCompensation(0F)
    cameraState.updateExposureCompensation(0F)

    setFlashMode(FlashMode.Off)
    cameraState.updateFlashMode(FlashMode.Off)

    setTorchEnabled(false)
    cameraState.updateTorchEnabled(false)

    setImageCaptureStrategy(cameraState.imageCaptureStrategy.value)
  }

  private fun updateConfig(
    captureMode: CaptureMode,
    captureModeChanged: Boolean = false,
    camSelectorChanged: Boolean = false,
  ) {
    resetConfig(captureMode.output)

    if (captureModeChanged || camSelectorChanged) {
      setCamFormat(
        camFormat = cameraState.camFormat.value,
        captureMode = captureMode,
      )
    }
  }


  private fun lockedLaunch(block: suspend CoroutineScope.() -> Unit): Job = cameraState.launch {
    sessionTopologyMutex.withLock {
      withContext(NonCancellable, block)
    }
  }
}
