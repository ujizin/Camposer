package com.ujizin.camposer.internal.core

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
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

internal expect class CameraEngineImpl : CameraEngine {
  override val cameraController: CameraController
  override val cameraState: CameraState
  override val cameraInfo: CameraInfo

  override fun resetConfig()

  override fun setCaptureMode(captureMode: CaptureMode)

  override fun removeCaptureMode(captureMode: CaptureMode)

  override fun setCamSelector(camSelector: CamSelector)

  override fun setCamFormat(camFormat: CamFormat)

  override fun setScaleType(scaleType: ScaleType)

  override fun setImplementationMode(implementationMode: ImplementationMode)

  override fun setMirrorMode(mirrorMode: MirrorMode)

  override fun disposeImageAnalyzer(imageAnalyzer: ImageAnalyzer?)

  override fun setImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean)

  override fun setImageAnalyzer(imageAnalyzer: ImageAnalyzer?)

  override fun setFrameRate(
    minFps: Int,
    maxFps: Int,
  )

  override fun setPinchToZoomEnabled(isPinchToZoomEnabled: Boolean)

  override fun setOrientationStrategy(orientationStrategy: OrientationStrategy)

  override fun setFlashMode(flashMode: FlashMode)

  override fun setTorchEnabled(isTorchEnabled: Boolean)

  override fun setExposureCompensation(exposureCompensation: Float)

  override fun setFocusOnTapEnabled(isFocusOnTapEnabled: Boolean)

  override fun setImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy)

  override fun isVideoStabilizationSupported(
    videoStabilizationMode: VideoStabilizationMode,
  ): Boolean

  override fun setVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode)

  override fun setZoomRatio(zoomRatio: Float)

  override fun isMirrorEnabled(): Boolean
}
