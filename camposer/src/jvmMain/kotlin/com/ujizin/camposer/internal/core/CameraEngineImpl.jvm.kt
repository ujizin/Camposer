package com.ujizin.camposer.internal.core

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.capture.JvmCameraCapture
import com.ujizin.camposer.internal.capture.JvmCameraCaptureImpl
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
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_EXPOSURE
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FPS
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_ZOOM
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

  actual override fun updateCaptureMode(captureMode: CaptureMode) {
    if (cameraState.captureMode.value == captureMode) return
    cameraState.updateCaptureMode(captureMode)
  }

  actual override fun updateCamSelector(camSelector: CamSelector) {
    if (cameraState.camSelector.value == camSelector) return
    capture.release()
    capture.open(camSelector.deviceIndex)
    cameraState.updateCamSelector(camSelector)
  }

  actual override fun updateScaleType(scaleType: ScaleType) {
    if (cameraState.scaleType.value == scaleType) return
    cameraState.updateScaleType(scaleType)
  }

  actual override fun updateFlashMode(flashMode: FlashMode) {
    if (cameraState.flashMode.value == flashMode) return
    // Flash is not supported on JVM/desktop — update state only
    cameraState.updateFlashMode(flashMode)
  }

  actual override fun updateMirrorMode(mirrorMode: MirrorMode) {
    if (cameraState.mirrorMode.value == mirrorMode) return
    cameraState.updateMirrorMode(mirrorMode)
  }

  actual override fun updateCamFormat(camFormat: CamFormat) {
    if (cameraState.camFormat.value == camFormat) return
    cameraState.updateCamFormat(camFormat)
  }

  actual override fun updateImplementationMode(implementationMode: ImplementationMode) {
    if (cameraState.implementationMode.value == implementationMode) return
    cameraState.updateImplementationMode(implementationMode)
  }

  actual override fun updateImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    if (cameraState.imageAnalyzer.value == imageAnalyzer) return
    cameraState.updateImageAnalyzer(imageAnalyzer)
  }

  actual override fun updateImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean) {
    if (cameraState.isImageAnalyzerEnabled.value == isImageAnalyzerEnabled) return
    cameraState.updateImageAnalyzerEnabled(isImageAnalyzerEnabled)
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
    capture.set(CAP_PROP_EXPOSURE, clamped.toDouble())
    cameraState.updateExposureCompensation(exposureCompensation)
  }

  actual override fun updateImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy) {
    if (cameraState.imageCaptureStrategy.value == imageCaptureStrategy) return
    cameraState.updateImageCaptureStrategy(imageCaptureStrategy)
  }

  actual override fun updateZoomRatio(zoomRatio: Float) {
    val cameraInfoState = cameraInfo.state.value
    val clamped = zoomRatio.coerceIn(
      minimumValue = cameraInfoState.minZoom,
      maximumValue = cameraInfoState.maxZoom,
    )
    if (cameraState.zoomRatio.value == clamped) return
    capture.set(CAP_PROP_ZOOM, clamped.toDouble())
    cameraState.updateZoomRatio(zoomRatio)
  }

  actual override fun updateFocusOnTapEnabled(isFocusOnTapEnabled: Boolean) {
    if (cameraState.isFocusOnTapEnabled.value == isFocusOnTapEnabled) return
    // Focus-on-tap is not supported on JVM/desktop — update state only
    cameraState.updateFocusOnTapEnabled(isFocusOnTapEnabled)
  }

  actual override fun updateTorchEnabled(isTorchEnabled: Boolean) {
    if (cameraState.isTorchEnabled.value == isTorchEnabled) return
    // Torch is not supported on JVM/desktop — update state only
    cameraState.updateTorchEnabled(isTorchEnabled)
  }

  actual override fun updateOrientationStrategy(orientationStrategy: OrientationStrategy) {
    if (cameraState.orientationStrategy.value == orientationStrategy) return
    cameraState.updateOrientationStrategy(orientationStrategy)
  }

  actual override fun updateFrameRate(frameRate: Int) {
    if (cameraState.frameRate.value == frameRate) return
    capture.set(CAP_PROP_FPS, frameRate.toDouble())
    cameraState.updateFrameRate(frameRate)
  }

  actual override fun updateVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode) {
    if (cameraState.videoStabilizationMode.value == videoStabilizationMode) return
    // Video stabilization is not supported on JVM/desktop — update state only
    cameraState.updateVideoStabilizationMode(videoStabilizationMode)
  }

  actual override fun isMirrorEnabled(): Boolean =
    when (cameraState.mirrorMode.value) {
      MirrorMode.On -> true
      MirrorMode.Off -> false
      MirrorMode.OnlyInFront -> cameraState.camSelector.value.camPosition == CamPosition.Front
    }
}
