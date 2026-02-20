package com.ujizin.camposer.internal.core

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.applier.AnalyzerApplier
import com.ujizin.camposer.internal.core.applier.CameraStateApplier
import com.ujizin.camposer.internal.core.applier.ExposureZoomApplier
import com.ujizin.camposer.internal.core.applier.PreviewApplier
import com.ujizin.camposer.internal.core.applier.SessionTopologyApplier
import com.ujizin.camposer.internal.core.applier.VideoApplier
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
import com.ujizin.camposer.state.properties.isMirrorEnabled
import com.ujizin.camposer.state.properties.selector.CamSelector
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
internal actual class CameraEngineImpl(
  actual override val cameraController: CameraController,
  override val iOSCameraController: IOSCameraController,
  actual override val cameraInfo: CameraInfo,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IOSCameraEngine {
  actual override val cameraState = CameraState(
    cameraInfo = cameraInfo,
    dispatcher = dispatcher,
  )

  private val sessionTopologyApplier = SessionTopologyApplier(
    cameraState = cameraState,
    cameraInfo = cameraInfo,
    iOSCameraController = iOSCameraController,
  )

  private val previewApplier = PreviewApplier(
    cameraState = cameraState,
    iOSCameraController = iOSCameraController,
  )

  private val analyzerApplier = AnalyzerApplier(
    cameraState = cameraState,
  )

  private val exposureZoomApplier = ExposureZoomApplier(
    cameraState = cameraState,
    iOSCameraController = iOSCameraController,
  )

  private val videoApplier = VideoApplier(
    cameraState = cameraState,
    iOSCameraController = iOSCameraController,
  )

  private val appliers: List<CameraStateApplier> = listOf(
    sessionTopologyApplier,
    previewApplier,
    analyzerApplier,
    exposureZoomApplier,
    videoApplier,
  )

  init {
    appliers.forEach(CameraStateApplier::onCameraInitialized)
  }

  actual override fun updateCaptureMode(captureMode: CaptureMode) {
    sessionTopologyApplier.applyCaptureMode(captureMode)
  }

  actual override fun updateCamSelector(camSelector: CamSelector) {
    sessionTopologyApplier.applyCamSelector(camSelector = camSelector)
  }

  actual override fun updateScaleType(scaleType: ScaleType) {
    if (cameraState.scaleType.value == scaleType) return
    previewApplier.applyScaleType(scaleType)
  }

  actual override fun updateFlashMode(flashMode: FlashMode) {
    if (cameraState.flashMode.value == flashMode) return
    exposureZoomApplier.applyFlashMode(flashMode)
  }

  actual override fun updateMirrorMode(mirrorMode: MirrorMode) {
    if (cameraState.mirrorMode.value == mirrorMode) return
    cameraState.updateMirrorMode(mirrorMode)
  }

  actual override fun updateCamFormat(camFormat: CamFormat) {
    if (cameraState.camFormat.value == camFormat) return
    sessionTopologyApplier.applyCamFormat(camFormat = camFormat)
  }

  actual override fun updateImplementationMode(implementationMode: ImplementationMode) {
    if (cameraState.implementationMode.value == implementationMode) return
    cameraState.updateImplementationMode(implementationMode)
  }

  actual override fun updateImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    if (cameraState.imageAnalyzer.value == imageAnalyzer) return
    analyzerApplier.applyImageAnalyzer(imageAnalyzer)
  }

  actual override fun updateImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean) {
    if (cameraState.isImageAnalyzerEnabled.value == isImageAnalyzerEnabled) return
    analyzerApplier.applyImageAnalyzerEnabled(isImageAnalyzerEnabled)
  }

  actual override fun updatePinchToZoomEnabled(isPinchToZoomEnabled: Boolean) {
    if (cameraState.isPinchToZoomEnabled.value == isPinchToZoomEnabled) return
    cameraState.updatePinchToZoomEnabled(isPinchToZoomEnabled)
  }

  actual override fun updateExposureCompensation(exposureCompensation: Float) {
    val clampedExposureCompensation = exposureCompensation.coerceIn(
      minimumValue = cameraInfo.minExposure,
      maximumValue = cameraInfo.maxExposure,
    )
    if (cameraState.exposureCompensation.value == clampedExposureCompensation) return
    exposureZoomApplier.applyExposureCompensation(clampedExposureCompensation)
  }

  actual override fun updateImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy) {
    if (cameraState.imageCaptureStrategy.value == imageCaptureStrategy) return
    videoApplier.applyImageCaptureStrategy(imageCaptureStrategy)
  }

  actual override fun updateZoomRatio(zoomRatio: Float) {
    val clampedZoomRatio = zoomRatio.coerceIn(
      minimumValue = cameraInfo.minZoom,
      maximumValue = cameraInfo.maxZoom,
    )
    if (cameraState.zoomRatio.value == clampedZoomRatio) return
    exposureZoomApplier.applyZoomRatio(clampedZoomRatio)
  }

  actual override fun updateFocusOnTapEnabled(isFocusOnTapEnabled: Boolean) {
    if (cameraState.isFocusOnTapEnabled.value == isFocusOnTapEnabled) return
    cameraState.updateFocusOnTapEnabled(isFocusOnTapEnabled)
  }

  actual override fun updateTorchEnabled(isTorchEnabled: Boolean) {
    if (cameraState.isTorchEnabled.value == isTorchEnabled) return
    exposureZoomApplier.applyTorchEnabled(isTorchEnabled)
  }

  actual override fun updateOrientationStrategy(orientationStrategy: OrientationStrategy) {
    if (cameraState.orientationStrategy.value == orientationStrategy) return
    cameraState.updateOrientationStrategy(orientationStrategy)
  }

  actual override fun updateFrameRate(frameRate: Int) {
    if (cameraState.frameRate.value == frameRate) return
    videoApplier.applyFrameRate(frameRate)
  }

  actual override fun updateVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode) {
    if (cameraState.videoStabilizationMode.value == videoStabilizationMode) return
    videoApplier.applyVideoStabilizationMode(videoStabilizationMode)
  }

  actual override fun isMirrorEnabled(): Boolean =
    cameraState.mirrorMode.value.isMirrorEnabled(iOSCameraController.getCurrentPosition())

  override fun getCurrentVideoOrientation() =
    when (cameraState.orientationStrategy.value) {
      OrientationStrategy.Device -> iOSCameraController.getCurrentDeviceOrientation()
      OrientationStrategy.Preview -> UIApplication.sharedApplication.statusBarOrientation
    }.toVideoOrientation()
}
