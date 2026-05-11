package com.ujizin.camposer.internal.core

import com.ujizin.camposer.internal.core.applier.AnalyzerApplier
import com.ujizin.camposer.internal.core.applier.CameraStateApplier
import com.ujizin.camposer.internal.core.applier.ExposureZoomApplier
import com.ujizin.camposer.internal.core.applier.PreviewApplier
import com.ujizin.camposer.internal.core.applier.SessionTopologyApplier
import com.ujizin.camposer.internal.core.applier.VideoApplier
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

internal abstract class CameraEngineCore : CameraEngine {
  protected abstract val sessionTopologyApplier: SessionTopologyApplier
  protected abstract val previewApplier: PreviewApplier
  protected abstract val analyzerApplier: AnalyzerApplier
  protected abstract val exposureZoomApplier: ExposureZoomApplier
  protected abstract val videoApplier: VideoApplier

  private val appliers: List<CameraStateApplier> by lazy {
    listOf(
      sessionTopologyApplier,
      previewApplier,
      analyzerApplier,
      exposureZoomApplier,
      videoApplier,
    )
  }

  open fun onCameraInitialized() {
    appliers.forEach(CameraStateApplier::onCameraInitialized)
  }

  open fun onCameraResumed() {
    appliers.forEach(CameraStateApplier::onCameraResumed)
  }

  open fun onCameraPaused() {
    appliers.forEach(CameraStateApplier::onCameraPaused)
  }

  override fun updateCaptureMode(captureMode: CaptureMode) {
    if (cameraState.captureMode.value == captureMode) return
    sessionTopologyApplier.applyCaptureMode(captureMode)
  }

  override fun updateCamSelector(camSelector: CamSelector) {
    if (cameraState.camSelector.value == camSelector) return
    sessionTopologyApplier.applyCamSelector(camSelector)
  }

  override fun updateScaleType(scaleType: ScaleType) {
    if (cameraState.scaleType.value == scaleType) return
    previewApplier.applyScaleType(scaleType)
  }

  override fun updateFlashMode(flashMode: FlashMode) {
    if (cameraState.flashMode.value == flashMode) return
    exposureZoomApplier.applyFlashMode(flashMode)
  }

  override fun updateMirrorMode(mirrorMode: MirrorMode) {
    if (cameraState.mirrorMode.value == mirrorMode) return
    previewApplier.applyMirrorMode(mirrorMode)
  }

  override fun updateCamFormat(camFormat: CamFormat) {
    if (cameraState.camFormat.value == camFormat) return
    sessionTopologyApplier.applyCamFormat(camFormat)
  }

  override fun updateImplementationMode(implementationMode: ImplementationMode) {
    if (cameraState.implementationMode.value == implementationMode) return
    cameraState.updateImplementationMode(implementationMode)
  }

  override fun updateImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    if (cameraState.imageAnalyzer.value == imageAnalyzer) return
    analyzerApplier.applyImageAnalyzer(imageAnalyzer)
  }

  override fun updateImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean) {
    if (cameraState.isImageAnalyzerEnabled.value == isImageAnalyzerEnabled) return
    analyzerApplier.applyImageAnalyzerEnabled(isImageAnalyzerEnabled)
  }

  override fun updatePinchToZoomEnabled(isPinchToZoomEnabled: Boolean) {
    if (cameraState.isPinchToZoomEnabled.value == isPinchToZoomEnabled) return
    cameraState.updatePinchToZoomEnabled(isPinchToZoomEnabled)
  }

  override fun updateExposureCompensation(exposureCompensation: Float) {
    val cameraInfoState = cameraInfo.state.value
    val clamped = exposureCompensation.coerceIn(
      minimumValue = cameraInfoState.minExposure,
      maximumValue = cameraInfoState.maxExposure,
    )
    if (cameraState.exposureCompensation.value == clamped) return
    exposureZoomApplier.applyExposureCompensation(clamped)
  }

  override fun updateImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy) {
    if (cameraState.imageCaptureStrategy.value == imageCaptureStrategy) return
    videoApplier.applyImageCaptureStrategy(imageCaptureStrategy)
  }

  override fun updateZoomRatio(zoomRatio: Float) {
    val cameraInfoState = cameraInfo.state.value
    val clamped = zoomRatio.coerceIn(
      minimumValue = cameraInfoState.minZoom,
      maximumValue = cameraInfoState.maxZoom,
    )
    if (cameraState.zoomRatio.value == clamped) return
    exposureZoomApplier.applyZoomRatio(clamped)
  }

  override fun updateFocusOnTapEnabled(isFocusOnTapEnabled: Boolean) {
    if (cameraState.isFocusOnTapEnabled.value == isFocusOnTapEnabled) return
    previewApplier.applyFocusOnTapEnabled(isFocusOnTapEnabled)
  }

  override fun updateTorchEnabled(isTorchEnabled: Boolean) {
    if (cameraState.isTorchEnabled.value == isTorchEnabled) return
    exposureZoomApplier.applyTorchEnabled(isTorchEnabled)
  }

  override fun updateOrientationStrategy(orientationStrategy: OrientationStrategy) {
    if (cameraState.orientationStrategy.value == orientationStrategy) return
    cameraState.updateOrientationStrategy(orientationStrategy)
  }

  override fun updateFrameRate(frameRate: Int) {
    if (cameraState.frameRate.value == frameRate) return
    videoApplier.applyFrameRate(frameRate)
  }

  override fun updateVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode) {
    if (cameraState.videoStabilizationMode.value == videoStabilizationMode) return
    videoApplier.applyVideoStabilizationMode(videoStabilizationMode)
  }
}
