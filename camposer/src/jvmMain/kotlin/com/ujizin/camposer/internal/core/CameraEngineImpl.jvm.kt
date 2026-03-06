package com.ujizin.camposer.internal.core

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.capture.JvmCameraCapture
import com.ujizin.camposer.internal.core.applier.AnalyzerApplier
import com.ujizin.camposer.internal.core.applier.CameraStateApplier
import com.ujizin.camposer.internal.core.applier.ExposureZoomApplier
import com.ujizin.camposer.internal.core.applier.PreviewApplier
import com.ujizin.camposer.internal.core.applier.SessionTopologyApplier
import com.ujizin.camposer.internal.core.applier.VideoApplier
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
import com.ujizin.camposer.state.properties.selector.CamPosition
import com.ujizin.camposer.state.properties.selector.CamSelector
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.bytedeco.opencv.opencv_core.Mat

internal actual class CameraEngineImpl(
  actual override val cameraController: CameraController,
  actual override val cameraInfo: CameraInfo,
  override val capture: JvmCameraCapture,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : JvmCameraEngine {
  actual override val cameraState: CameraState = CameraState(
    cameraInfo = cameraInfo,
    dispatcher = dispatcher,
  )

  override var currentMat: Mat? = null

  private val sessionTopologyApplier = SessionTopologyApplier(
    cameraState = cameraState,
  )

  private val previewApplier = PreviewApplier(
    cameraState = cameraState,
  )

  private val analyzerApplier = AnalyzerApplier(
    cameraState = cameraState,
  )

  private val exposureZoomApplier = ExposureZoomApplier(
    cameraState = cameraState,
    capture = capture,
  )

  private val videoApplier = VideoApplier(
    cameraState = cameraState,
    capture = capture,
  )

  private val appliers: List<CameraStateApplier> = listOf(
    sessionTopologyApplier,
    previewApplier,
    analyzerApplier,
    exposureZoomApplier,
    videoApplier,
  )

  actual override fun updateCaptureMode(captureMode: CaptureMode) {
    if (cameraState.captureMode.value == captureMode) return
    sessionTopologyApplier.applyCaptureMode(captureMode)
  }

  actual override fun updateCamSelector(camSelector: CamSelector) {
    if (cameraState.camSelector.value == camSelector) return
    sessionTopologyApplier.applyCamSelector(camSelector)
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
    previewApplier.applyMirrorMode(mirrorMode)
  }

  actual override fun updateCamFormat(camFormat: CamFormat) {
    if (cameraState.camFormat.value == camFormat) return
    sessionTopologyApplier.applyCamFormat(camFormat)
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
    val cameraInfoState = cameraInfo.state.value
    val clamped = exposureCompensation.coerceIn(
      minimumValue = cameraInfoState.minExposure,
      maximumValue = cameraInfoState.maxExposure,
    )
    if (cameraState.exposureCompensation.value == clamped) return
    exposureZoomApplier.applyExposureCompensation(clamped)
  }

  actual override fun updateImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy) {
    if (cameraState.imageCaptureStrategy.value == imageCaptureStrategy) return
    videoApplier.applyImageCaptureStrategy(imageCaptureStrategy)
  }

  actual override fun updateZoomRatio(zoomRatio: Float) {
    val cameraInfoState = cameraInfo.state.value
    val clamped = zoomRatio.coerceIn(
      minimumValue = cameraInfoState.minZoom,
      maximumValue = cameraInfoState.maxZoom,
    )
    if (cameraState.zoomRatio.value == clamped) return
    exposureZoomApplier.applyZoomRatio(clamped)
  }

  actual override fun updateFocusOnTapEnabled(isFocusOnTapEnabled: Boolean) {
    if (cameraState.isFocusOnTapEnabled.value == isFocusOnTapEnabled) return
    previewApplier.applyFocusOnTapEnabled(isFocusOnTapEnabled)
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
    when (cameraState.mirrorMode.value) {
      MirrorMode.On -> true
      MirrorMode.Off -> false
      MirrorMode.OnlyInFront -> cameraState.camSelector.value.camPosition == CamPosition.Front
    }
}
