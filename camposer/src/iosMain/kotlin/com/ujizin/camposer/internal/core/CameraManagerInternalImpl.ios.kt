package com.ujizin.camposer.internal.core

import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.internal.extensions.toVideoOrientation
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.ImplementationMode
import com.ujizin.camposer.state.properties.MirrorMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.selector.CamSelector
import com.ujizin.camposer.state.properties.selector.getCaptureDevice
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
internal actual class CameraManagerInternalImpl(
  override val cameraController: IOSCameraController,
  internal val cameraInfo: CameraInfo,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IOSCameraManagerInternal {
  actual override val cameraState = CameraState(
    cameraInfo = cameraInfo,
    dispatcher = dispatcher,
    cameraDelegate = this,
  )

  init {
    cameraController.setPreviewGravity(cameraState.scaleType.gravity)
  }

  actual override fun isMirrorEnabled(): Boolean =
    cameraState.mirrorMode.isMirrorEnabled(cameraController.getCurrentPosition())

  actual override fun setCaptureMode(captureMode: CaptureMode) {
    cameraController.addOutput(captureMode.output)
    updateConfig(captureModeChanged = true)
  }

  actual override fun removeCaptureMode(captureMode: CaptureMode) {
    cameraController.removeOutput(captureMode.output)
  }

  actual override fun setCamSelector(camSelector: CamSelector) {
    cameraController.setCaptureDevice(cameraController.getCaptureDevice(camSelector))
    updateConfig(camSelectorChanged = true)
  }

  actual override fun setCamFormat(camFormat: CamFormat) {
    camFormat.applyConfigs(
      cameraInfo = cameraInfo,
      iosCameraController = cameraController,
      onDeviceFormatUpdated = { cameraInfo.rebind(output = cameraState.captureMode.output) },
      onStabilizationModeChanged = ::setVideoStabilizationMode,
      onFrameRateChanged = ::setFrameRate,
    )
  }

  actual override fun setScaleType(scaleType: ScaleType) {
    cameraController.setPreviewGravity(scaleType.gravity)
  }

  actual override fun setImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    imageAnalyzer?.isEnabled = cameraState.isImageAnalyzerEnabled
  }

  actual override fun setImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean) {
    cameraState.imageAnalyzer?.isEnabled = isImageAnalyzerEnabled
  }

  actual override fun disposeImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    imageAnalyzer?.dispose()
  }

  actual override fun setFrameRate(
    minFps: Int,
    maxFps: Int,
  ) {
    when {
      cameraState.frameRate != minFps -> cameraState.frameRate = minFps
      else -> cameraController.setFrameRate(minFps)
    }
  }

  actual override fun setFlashMode(flashMode: FlashMode) {
    cameraController.setFlashMode(flashMode.mode)
  }

  actual override fun setTorchEnabled(isTorchEnabled: Boolean) {
    cameraController.setTorchEnabled(isTorchEnabled)
  }

  actual override fun setExposureCompensation(exposureCompensation: Float) {
    cameraController.setExposureCompensation(exposureCompensation)
  }

  actual override fun setImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy) {
    cameraController.setCameraOutputQuality(
      quality = imageCaptureStrategy.quality,
      highResolutionEnabled = imageCaptureStrategy.highResolutionEnabled,
    )
  }

  actual override fun isVideoStabilizationSupported(
    videoStabilizationMode: VideoStabilizationMode,
  ): Boolean = cameraController.isVideoStabilizationSupported(videoStabilizationMode.value)

  actual override fun setVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode) {
    when {
      videoStabilizationMode != cameraState.videoStabilizationMode -> {
        cameraState.videoStabilizationMode = videoStabilizationMode
      }

      else -> {
        cameraController.setVideoStabilization(videoStabilizationMode.value)
      }
    }
  }

  actual override fun setZoomRatio(zoomRatio: Float) {
    cameraController.setZoomRatio(zoomRatio)
  }

  actual override fun resetConfig() =
    with(cameraState) {
      cameraInfo.rebind(output = captureMode.output)

      zoomRatio = cameraInfo.minZoom
      exposureCompensation = 0F
      flashMode = FlashMode.Off
      isTorchEnabled = false
      cameraController.setCameraOutputQuality(
        quality = imageCaptureStrategy.quality,
        highResolutionEnabled = imageCaptureStrategy.highResolutionEnabled,
      )
    }

  actual override fun setImplementationMode(implementationMode: ImplementationMode) {
    // no-op
  }

  actual override fun setMirrorMode(mirrorMode: MirrorMode) {
    // no-op
  }

  actual override fun setFocusOnTapEnabled(isFocusOnTapEnabled: Boolean) {
    // no-op
  }

  actual override fun setPinchToZoomEnabled(isPinchToZoomEnabled: Boolean) {
    // no-op
  }

  actual override fun setOrientationStrategy(orientationStrategy: OrientationStrategy) {
    // no-op
  }

  override fun getCurrentVideoOrientation() =
    when (cameraState.orientationStrategy) {
      OrientationStrategy.Device -> cameraController.getCurrentDeviceOrientation()
      OrientationStrategy.Preview -> UIApplication.sharedApplication.statusBarOrientation
    }.toVideoOrientation()

  private fun updateConfig(
    captureModeChanged: Boolean = false,
    camSelectorChanged: Boolean = false,
  ) {
    resetConfig()

    if (captureModeChanged || camSelectorChanged) {
      setCamFormat(cameraState.camFormat)
    }
  }
}
