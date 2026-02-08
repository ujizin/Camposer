package com.ujizin.camposer.internal.core

import android.content.ContentResolver
import android.util.Range
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.MirrorMode.MIRROR_MODE_OFF
import androidx.camera.core.MirrorMode.MIRROR_MODE_ON
import androidx.camera.view.CameraController.IMAGE_ANALYSIS
import androidx.compose.ui.util.fastCoerceIn
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.camerax.CameraXController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.ImageCaptureStrategy.MinLatency
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
import kotlin.math.roundToInt

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
    cameraDelegate = this,
    dispatcher = dispatcher,
  )

  actual override fun isMirrorEnabled(): Boolean =
    when (cameraXController.videoCaptureMirrorMode) {
      MIRROR_MODE_ON -> true
      MIRROR_MODE_OFF -> false
      else -> cameraState.camSelector.camPosition == CamPosition.Front
    }

  override fun onCameraInitialized() {
    cameraXController.lifecycleOwner.lifecycle.addObserver(CameraConfigSaver())
    with(cameraXController) {
      setEnabledUseCases(getUseCases())
      setZoomRatio(cameraState.zoomRatio)
      isTapToFocusEnabled = cameraState.isFocusOnTapEnabled
      videoCaptureMirrorMode = cameraState.mirrorMode.mode
      setCamSelector(cameraState.camSelector)
    }
  }

  actual override fun setCaptureMode(captureMode: CaptureMode) {
    cameraXController.setEnabledUseCases(getUseCases(captureMode))
  }

  actual override fun setCamSelector(camSelector: CamSelector) {
    mainExecutor.execute {
      cameraXController.cameraSelector = camSelector.selector
      cameraInfo.rebind()
      resetConfig()
    }
  }

  actual override fun setCamFormat(camFormat: CamFormat) {
    camFormat.applyConfigs(
      cameraInfo = cameraInfo,
      controller = cameraXController,
      onFrameRateChanged = ::setFrameRate,
      onStabilizationModeChanged = ::setVideoStabilizationMode,
    )
  }

  actual override fun setMirrorMode(mirrorMode: MirrorMode) {
    cameraXController.videoCaptureMirrorMode = mirrorMode.mode
  }

  actual override fun setImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    cameraXController.setImageAnalysisAnalyzer(
      cameraXController.mainExecutor,
      imageAnalyzer?.analyzer ?: return,
    )
  }

  actual override fun setImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean) {
    cameraXController.setEnabledUseCases(
      getUseCases(isImageAnalyzerEnabled = isImageAnalyzerEnabled),
    )
  }

  actual override fun setFrameRate(
    minFps: Int,
    maxFps: Int,
  ) {
    when {
      cameraState.frameRate != minFps -> cameraState.frameRate = minFps
      else -> cameraXController.videoCaptureTargetFrameRate = Range(minFps, maxFps)
    }
  }

  actual override fun setFlashMode(flashMode: FlashMode) {
    cameraXController.imageCaptureFlashMode = flashMode.mode
  }

  actual override fun setTorchEnabled(isTorchEnabled: Boolean) {
    cameraXController.enableTorch(isTorchEnabled)
  }

  actual override fun setExposureCompensation(exposureCompensation: Float) {
    cameraXController.setExposureCompensationIndex(
      exposureCompensation.roundToInt(),
    )
  }

  actual override fun setFocusOnTapEnabled(isFocusOnTapEnabled: Boolean) {
    cameraXController.isTapToFocusEnabled = isFocusOnTapEnabled
  }

  @OptIn(ExperimentalZeroShutterLag::class)
  actual override fun setImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy) {
    val isZSLSupported = cameraInfo.isZeroShutterLagSupported
    val mode = when {
      imageCaptureStrategy == MinLatency && !isZSLSupported -> imageCaptureStrategy.fallback
      else -> imageCaptureStrategy.mode
    }
    cameraXController.imageCaptureMode = mode
  }

  actual override fun isVideoStabilizationSupported(
    videoStabilizationMode: VideoStabilizationMode,
  ): Boolean = false // TODO CameraX controller does not support yet :(

  actual override fun setVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode) {
    if (cameraState.videoStabilizationMode != videoStabilizationMode) {
      cameraState.videoStabilizationMode = videoStabilizationMode
      return
    }

    // TODO CameraX controller does not support yet :(
  }

  actual override fun setZoomRatio(zoomRatio: Float) {
    cameraXController.setZoomRatio(zoomRatio)
  }

  actual override fun resetConfig() =
    with(cameraState) {
      zoomRatio = cameraInfo.minZoom
      exposureCompensation = 0F
      flashMode = FlashMode.Off
      isTorchEnabled = false
    }

  actual override fun setPinchToZoomEnabled(isPinchToZoomEnabled: Boolean) {
    // no-op
  }

  actual override fun setScaleType(scaleType: ScaleType) {
    // no-op
  }

  actual override fun setImplementationMode(implementationMode: ImplementationMode) {
    // no-op
  }

  actual override fun disposeImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    // no-op
  }

  actual override fun removeCaptureMode(captureMode: CaptureMode) {
    // no-op
  }

  actual override fun setOrientationStrategy(orientationStrategy: OrientationStrategy) {
    // no-op
  }

  private fun getUseCases(
    mode: CaptureMode = cameraState.captureMode,
    isImageAnalyzerEnabled: Boolean = cameraState.isImageAnalyzerEnabled,
  ): Int =
    when {
      isImageAnalyzerEnabled && mode != CaptureMode.Video -> mode.value or IMAGE_ANALYSIS
      else -> mode.value
    }

  internal inner class CameraConfigSaver : DefaultLifecycleObserver {
    private var hasPaused: Boolean = false

    override fun onResume(owner: LifecycleOwner) {
      super.onResume(owner)
      if (!hasPaused) return
      setZoomRatio(cameraState.zoomRatio.fastCoerceIn(cameraInfo.minZoom, cameraInfo.maxZoom))
      setExposureCompensation(cameraState.exposureCompensation)
    }

    override fun onPause(owner: LifecycleOwner) {
      hasPaused = true
      super.onPause(owner)
    }
  }
}
