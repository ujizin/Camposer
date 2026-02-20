package com.ujizin.camposer.state

import androidx.compose.ui.util.fastCoerceIn
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.session.CameraSession
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
import com.ujizin.camposer.state.properties.format.Default
import com.ujizin.camposer.state.properties.selector.CamSelector
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A state holder for the Camera composition.
 *
 * This class contains all the mutable properties that define the current configuration
 * of the camera, such as the camera selector (front/back), capture mode (image/video),
 * flash settings, zoom levels, exposure compensation, and feature toggles like pinch-to-zoom
 * or focus-on-tap.
 */
public class CameraState internal constructor(
  private val cameraInfo: CameraInfo,
  dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
  private val stateScope = CoroutineScope(dispatcher + SupervisorJob())

  private val _captureMode = MutableStateFlow(CaptureMode.Image)
  public val captureMode: StateFlow<CaptureMode> = _captureMode.asStateFlow()

  private val _camSelector = MutableStateFlow(CamSelector.Back)
  public val camSelector: StateFlow<CamSelector> = _camSelector.asStateFlow()

  private val _scaleType = MutableStateFlow(ScaleType.FillCenter)
  public val scaleType: StateFlow<ScaleType> = _scaleType.asStateFlow()

  private val _flashMode = MutableStateFlow(FlashMode.Off)
  public val flashMode: StateFlow<FlashMode> = _flashMode.asStateFlow()

  private val _mirrorMode = MutableStateFlow(MirrorMode.OnlyInFront)
  public val mirrorMode: StateFlow<MirrorMode> = _mirrorMode.asStateFlow()

  private val _camFormat = MutableStateFlow(CamFormat.Default)
  public val camFormat: StateFlow<CamFormat> = _camFormat.asStateFlow()

  private val _implementationMode = MutableStateFlow(ImplementationMode.Performance)
  public val implementationMode: StateFlow<ImplementationMode> = _implementationMode.asStateFlow()

  private val _imageAnalyzer = MutableStateFlow<ImageAnalyzer?>(null)
  public val imageAnalyzer: StateFlow<ImageAnalyzer?> = _imageAnalyzer.asStateFlow()

  private val _isImageAnalyzerEnabled = MutableStateFlow(_imageAnalyzer.value != null)
  public val isImageAnalyzerEnabled: StateFlow<Boolean> = _isImageAnalyzerEnabled.asStateFlow()

  private val _isPinchToZoomEnabled = MutableStateFlow(true)
  public val isPinchToZoomEnabled: StateFlow<Boolean> = _isPinchToZoomEnabled.asStateFlow()

  private val _exposureCompensation = MutableStateFlow(0F)
  public val exposureCompensation: StateFlow<Float> = _exposureCompensation.asStateFlow()

  private val _imageCaptureStrategy = MutableStateFlow(ImageCaptureStrategy.Balanced)
  public val imageCaptureStrategy: StateFlow<ImageCaptureStrategy> =
    _imageCaptureStrategy.asStateFlow()

  private val _zoomRatio = MutableStateFlow(cameraInfo.minZoom)
  public val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

  private val _isFocusOnTapEnabled = MutableStateFlow(true)
  public val isFocusOnTapEnabled: StateFlow<Boolean> = _isFocusOnTapEnabled.asStateFlow()

  private val _isTorchEnabled = MutableStateFlow(false)
  public val isTorchEnabled: StateFlow<Boolean> = _isTorchEnabled.asStateFlow()

  private val _orientationStrategy = MutableStateFlow(OrientationStrategy.Device)
  public val orientationStrategy: StateFlow<OrientationStrategy> =
    _orientationStrategy.asStateFlow()

  private val _frameRate = MutableStateFlow(-1)
  public val frameRate: StateFlow<Int> = _frameRate.asStateFlow()

  private val _videoStabilizationMode = MutableStateFlow(VideoStabilizationMode.Off)
  public val videoStabilizationMode: StateFlow<VideoStabilizationMode> =
    _videoStabilizationMode.asStateFlow()

  internal fun launch(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit,
  ): Job =
    stateScope.launch(
      context = context,
      block = block,
    )

  internal fun updateCaptureMode(captureMode: CaptureMode) {
    _captureMode.update { captureMode }
  }

  internal fun updateCamSelector(camSelector: CamSelector) {
    _camSelector.update { camSelector }
  }

  internal fun updateScaleType(scaleType: ScaleType) {
    _scaleType.update { scaleType }
  }

  internal fun updateFlashMode(flashMode: FlashMode) {
    _flashMode.update { flashMode }
  }

  public fun updateMirrorMode(mirrorMode: MirrorMode) {
    _mirrorMode.update { mirrorMode }
  }

  internal fun updateCamFormat(camFormat: CamFormat) {
    _camFormat.update { camFormat }
  }

  internal fun updateImplementationMode(implementationMode: ImplementationMode) {
    _implementationMode.update { implementationMode }
  }

  internal fun updateImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    _imageAnalyzer.update { imageAnalyzer }
  }

  internal fun updateImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean) {
    _isImageAnalyzerEnabled.update { isImageAnalyzerEnabled }
  }

  internal fun updatePinchToZoomEnabled(isPinchToZoomEnabled: Boolean) {
    _isPinchToZoomEnabled.update { isPinchToZoomEnabled }
  }

  internal fun updateExposureCompensation(exposureCompensation: Float) {
    _exposureCompensation.update {
      exposureCompensation.fastCoerceIn(cameraInfo.minExposure, cameraInfo.maxExposure)
    }
  }

  internal fun updateImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy) {
    _imageCaptureStrategy.update { imageCaptureStrategy }
  }

  internal fun updateZoomRatio(zoomRatio: Float) {
    _zoomRatio.update { zoomRatio.fastCoerceIn(cameraInfo.minZoom, cameraInfo.maxZoom) }
  }

  internal fun updateFocusOnTapEnabled(isFocusOnTapEnabled: Boolean) {
    _isFocusOnTapEnabled.update { isFocusOnTapEnabled }
  }

  internal fun updateTorchEnabled(isTorchEnabled: Boolean) {
    _isTorchEnabled.update { isTorchEnabled }
  }

  internal fun updateOrientationStrategy(orientationStrategy: OrientationStrategy) {
    _orientationStrategy.update { orientationStrategy }
  }

  internal fun updateFrameRate(frameRate: Int) {
    _frameRate.update { frameRate }
  }

  internal fun updateVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode) {
    _videoStabilizationMode.update { videoStabilizationMode }
  }

  /**
   * Disposes this camera state and cancels all pending operations.
   * This should be called when the camera session is no longer needed.
   */
  internal fun dispose() {
    stateScope.cancel()
  }
}

internal fun CameraSession.update(
  camSelector: CamSelector,
  captureMode: CaptureMode,
  scaleType: ScaleType,
  isImageAnalysisEnabled: Boolean,
  imageAnalyzer: ImageAnalyzer?,
  implementationMode: ImplementationMode,
  isFocusOnTapEnabled: Boolean,
  imageCaptureStrategy: ImageCaptureStrategy,
  camFormat: CamFormat,
  isPinchToZoomEnabled: Boolean,
) {
  with(cameraEngine) {
    updateCaptureMode(captureMode)
    updateCamSelector(camSelector)
    updateCamFormat(camFormat)
    updateScaleType(scaleType)
    updateImageAnalyzer(imageAnalyzer)
    updateImageAnalyzerEnabled(isImageAnalysisEnabled)
    updateImplementationMode(implementationMode)
    updateFocusOnTapEnabled(isFocusOnTapEnabled)
    updateImageCaptureStrategy(imageCaptureStrategy)
    updatePinchToZoomEnabled(isPinchToZoomEnabled)

    state.launch { onSessionStarted() }
  }
}
