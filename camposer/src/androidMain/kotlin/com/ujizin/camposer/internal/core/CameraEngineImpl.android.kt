package com.ujizin.camposer.internal.core

import android.content.ContentResolver
import androidx.camera.core.MirrorMode.MIRROR_MODE_OFF
import androidx.camera.core.MirrorMode.MIRROR_MODE_ON
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.applier.AnalyzerApplier
import com.ujizin.camposer.internal.core.applier.CameraStateApplier
import com.ujizin.camposer.internal.core.applier.ExposureZoomApplier
import com.ujizin.camposer.internal.core.applier.PreviewApplier
import com.ujizin.camposer.internal.core.applier.SessionTopologyApplier
import com.ujizin.camposer.internal.core.applier.VideoApplier
import com.ujizin.camposer.internal.core.camerax.CameraXController
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
import java.util.concurrent.Executor

internal actual class CameraEngineImpl(
  actual override val cameraController: CameraController,
  actual override val cameraInfo: CameraInfo,
  override val cameraXController: CameraXController,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AndroidCameraEngine {
  override val mainExecutor: Executor
    get() = cameraXController.mainExecutor

  override val contentResolver: ContentResolver
    get() = cameraXController.contentResolver

  actual override val cameraState: CameraState = CameraState(
    cameraInfo = cameraInfo,
    dispatcher = dispatcher,
  )

  private val sessionTopologyApplier = SessionTopologyApplier(
    cameraState = cameraState,
    cameraInfo = cameraInfo,
    cameraXController = cameraXController,
  )

  private val previewApplier = PreviewApplier(
    cameraState = cameraState,
    cameraXController = cameraXController,
  )

  private val analyzerApplier = AnalyzerApplier(
    cameraState = cameraState,
    cameraXController = cameraXController,
  )

  private val exposureZoomApplier = ExposureZoomApplier(
    cameraState = cameraState,
    cameraXController = cameraXController,
  )

  private val videoApplier = VideoApplier(
    cameraInfo = cameraInfo,
    cameraState = cameraState,
    cameraXController = cameraXController,
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
    sessionTopologyApplier.applyCaptureMode(captureMode = captureMode)
  }

  actual override fun updateCamSelector(camSelector: CamSelector) {
    if (cameraState.camSelector.value == camSelector) return
    sessionTopologyApplier.applyCamSelector(camSelector)
  }

  actual override fun updateScaleType(scaleType: ScaleType) {
    if (cameraState.scaleType.value == scaleType) return
    cameraState.updateScaleType(scaleType)
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
    analyzerApplier.applyImageAnalyzerEnabled(isImageAnalyzerEnabled = isImageAnalyzerEnabled)
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
    when (cameraXController.videoCaptureMirrorMode) {
      MIRROR_MODE_ON -> true
      MIRROR_MODE_OFF -> false
      else -> cameraState.camSelector.value.camPosition == CamPosition.Front
    }

  override fun onCameraInitialized() {
    cameraXController.lifecycleOwner.lifecycle.addObserver(CameraLifecycleObserver())
    appliers.forEach(CameraStateApplier::onCameraInitialized)
  }

  internal inner class CameraLifecycleObserver : DefaultLifecycleObserver {
    override fun onResume(owner: LifecycleOwner) {
      super.onResume(owner)
      appliers.forEach(CameraStateApplier::onCameraResumed)
    }

    override fun onPause(owner: LifecycleOwner) {
      appliers.forEach(CameraStateApplier::onCameraPaused)
      super.onPause(owner)
    }
  }
}
