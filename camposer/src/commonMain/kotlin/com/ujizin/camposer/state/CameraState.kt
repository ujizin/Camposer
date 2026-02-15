package com.ujizin.camposer.state

import androidx.compose.ui.util.fastCoerceIn
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.CameraEngineDelegate
import com.ujizin.camposer.internal.utils.asyncDistinctConfig
import com.ujizin.camposer.internal.utils.distinctConfig
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
  private val cameraDelegate: CameraEngineDelegate,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
  private val cameraMutex = Mutex()
  private val stateScope = CoroutineScope(dispatcher + SupervisorJob())

  public var captureMode: CaptureMode by asyncDistinctConfig(
    scope = stateScope,
    mutex = cameraMutex,
    dispatcher = dispatcher,
    value = CaptureMode.Image,
    onDispose = cameraDelegate::removeCaptureMode,
    block = cameraDelegate::setCaptureMode,
  )
    internal set

  public var camSelector: CamSelector by asyncDistinctConfig(
    scope = stateScope,
    mutex = cameraMutex,
    dispatcher = dispatcher,
    value = CamSelector.Back,
    block = cameraDelegate::setCamSelector,
  )
    internal set

  public var scaleType: ScaleType by distinctConfig(
    value = ScaleType.FillCenter,
    block = cameraDelegate::setScaleType,
  )
    internal set

  public var flashMode: FlashMode by distinctConfig(
    value = FlashMode.Off,
    predicate = { old, new -> old != new },
    check = { check(it.isFlashAvailable()) { "Flash must be supported to be enabled" } },
    block = cameraDelegate::setFlashMode,
  )
    internal set

  public var mirrorMode: MirrorMode by distinctConfig(
    value = MirrorMode.OnlyInFront,
    block = cameraDelegate::setMirrorMode,
  )

  public var camFormat: CamFormat by distinctConfig(
    value = CamFormat.Default,
    block = cameraDelegate::setCamFormat,
  )
    internal set

  // No-op in iOS
  public var implementationMode: ImplementationMode by distinctConfig(
    value = ImplementationMode.Performance,
    block = cameraDelegate::setImplementationMode,
  )
    internal set

  public var imageAnalyzer: ImageAnalyzer? by distinctConfig(
    value = null,
    onDispose = cameraDelegate::disposeImageAnalyzer,
    block = cameraDelegate::setImageAnalyzer,
  )
    internal set

  public var isImageAnalyzerEnabled: Boolean by distinctConfig(
    value = imageAnalyzer != null,
    block = cameraDelegate::setImageAnalyzerEnabled,
  )
    internal set

  public var isPinchToZoomEnabled: Boolean by distinctConfig(
    value = true,
    block = cameraDelegate::setPinchToZoomEnabled,
  )
    internal set

  public var exposureCompensation: Float by distinctConfig(
    value = 0F,
    check = {
      check(cameraInfo.isExposureSupported) { "Exposure compensation must be supported to be set" }
    },
    onSet = { it.fastCoerceIn(cameraInfo.minExposure, cameraInfo.maxExposure) },
    block = cameraDelegate::setExposureCompensation,
  )
    internal set

  public var imageCaptureStrategy: ImageCaptureStrategy by distinctConfig(
    value = ImageCaptureStrategy.Balanced,
    block = cameraDelegate::setImageCaptureStrategy,
  )
    internal set

  public var zoomRatio: Float by distinctConfig(
    value = cameraInfo.minZoom,
    onSet = { it.fastCoerceIn(cameraInfo.minZoom, cameraInfo.maxZoom) },
    block = cameraDelegate::setZoomRatio,
  )
    internal set

  public var isFocusOnTapEnabled: Boolean by distinctConfig(
    value = true,
    cameraDelegate::setFocusOnTapEnabled,
  )
    internal set

  public var isTorchEnabled: Boolean by distinctConfig(
    value = false,
    check = {
      check((!it || cameraInfo.isTorchAvailable)) { "Torch must be supported to enable" }
    },
    predicate = { old, new -> old != new && (!new || cameraInfo.isTorchAvailable) },
    block = cameraDelegate::setTorchEnabled,
  )
    internal set

  public var orientationStrategy: OrientationStrategy by distinctConfig(
    value = OrientationStrategy.Device,
    block = cameraDelegate::setOrientationStrategy,
  )
    internal set

  public var frameRate: Int by distinctConfig(
    value = -1,
    check = {
      check(it in cameraInfo.minFPS..cameraInfo.maxFPS) {
        "FPS $it must be in range ${cameraInfo.minFPS..cameraInfo.maxFPS}"
      }
    },
    block = { cameraDelegate.setFrameRate(it, it) },
  )
    internal set

  public var videoStabilizationMode: VideoStabilizationMode by distinctConfig(
    value = VideoStabilizationMode.Off,
    check = {
      check(cameraDelegate.isVideoStabilizationSupported(it)) {
        "Video stabilization mode must be supported to enable"
      }
    },
    block = cameraDelegate::setVideoStabilizationMode,
  )
    internal set

  private fun FlashMode.isFlashAvailable() =
    this == FlashMode.Off || (cameraInfo.isFlashSupported && cameraInfo.isFlashAvailable)

  internal fun invokeWhenCompleted(block: () -> Unit) {
    stateScope.launch {
      cameraMutex.withLock { block() }
    }
  }

  /**
   * Disposes this camera state and cancels all pending operations.
   * This should be called when the camera session is no longer needed.
   */
  internal fun dispose() {
    stateScope.cancel()
    imageAnalyzer?.let { cameraDelegate.disposeImageAnalyzer(it) }
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
  with(state) {
    this.captureMode = captureMode
    this.camSelector = camSelector
    this.camFormat = camFormat
    this.scaleType = scaleType
    this.imageAnalyzer = imageAnalyzer
    this.isImageAnalyzerEnabled = isImageAnalysisEnabled
    this.implementationMode = implementationMode
    this.isFocusOnTapEnabled = isFocusOnTapEnabled
    this.imageCaptureStrategy = imageCaptureStrategy
    this.isPinchToZoomEnabled = isPinchToZoomEnabled

    state.invokeWhenCompleted(::onSessionStarted)
  }
}
