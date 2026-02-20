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

internal interface CameraEngine {
  val cameraController: CameraController

  val cameraState: CameraState

  val cameraInfo: CameraInfo

  fun updateCaptureMode(captureMode: CaptureMode)

  fun updateCamSelector(camSelector: CamSelector)

  fun updateScaleType(scaleType: ScaleType)

  fun updateFlashMode(flashMode: FlashMode)

  fun updateMirrorMode(mirrorMode: MirrorMode)

  fun updateCamFormat(camFormat: CamFormat)

  fun updateImplementationMode(implementationMode: ImplementationMode)

  fun updateImageAnalyzer(imageAnalyzer: ImageAnalyzer?)

  fun updateImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean)

  fun updatePinchToZoomEnabled(isPinchToZoomEnabled: Boolean)

  fun updateExposureCompensation(exposureCompensation: Float)

  fun updateImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy)

  fun updateZoomRatio(zoomRatio: Float)

  fun updateFocusOnTapEnabled(isFocusOnTapEnabled: Boolean)

  fun updateTorchEnabled(isTorchEnabled: Boolean)

  fun updateOrientationStrategy(orientationStrategy: OrientationStrategy)

  fun updateFrameRate(frameRate: Int)

  fun updateVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode)

  fun isMirrorEnabled(): Boolean
}
