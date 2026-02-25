package com.ujizin.camposer.fake

import com.ujizin.camposer.internal.core.CameraEngine
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
import kotlinx.coroutines.CoroutineDispatcher

internal expect class FakeCameraEngine(
  cameraTest: FakeCameraTest,
  testDispatcher: CoroutineDispatcher,
) : CameraEngine {
  override val cameraState: CameraState

  override fun updateCaptureMode(captureMode: CaptureMode)

  override fun updateCamSelector(camSelector: CamSelector)

  override fun updateScaleType(scaleType: ScaleType)

  override fun updateFlashMode(flashMode: FlashMode)

  override fun updateMirrorMode(mirrorMode: MirrorMode)

  override fun updateCamFormat(camFormat: CamFormat)

  override fun updateImplementationMode(implementationMode: ImplementationMode)

  override fun updateImageAnalyzer(imageAnalyzer: ImageAnalyzer?)

  override fun updateImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean)

  override fun updatePinchToZoomEnabled(isPinchToZoomEnabled: Boolean)

  override fun updateExposureCompensation(exposureCompensation: Float)

  override fun updateImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy)

  override fun updateZoomRatio(zoomRatio: Float)

  override fun updateFocusOnTapEnabled(isFocusOnTapEnabled: Boolean)

  override fun updateTorchEnabled(isTorchEnabled: Boolean)

  override fun updateOrientationStrategy(orientationStrategy: OrientationStrategy)

  override fun updateFrameRate(frameRate: Int)

  override fun updateVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode)

  override fun isMirrorEnabled(): Boolean
}
