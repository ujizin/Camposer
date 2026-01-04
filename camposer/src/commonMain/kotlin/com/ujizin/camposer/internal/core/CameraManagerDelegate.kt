package com.ujizin.camposer.internal.core

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

/**
 * A delegate interface for controlling camera operations and settings.
 */
internal interface CameraManagerDelegate {
  fun setCaptureMode(captureMode: CaptureMode)

  fun removeCaptureMode(captureMode: CaptureMode)

  fun setCamSelector(camSelector: CamSelector)

  fun setCamFormat(camFormat: CamFormat)

  fun setScaleType(scaleType: ScaleType)

  fun setImplementationMode(implementationMode: ImplementationMode)

  fun setMirrorMode(mirrorMode: MirrorMode)

  fun disposeImageAnalyzer(imageAnalyzer: ImageAnalyzer?)

  fun setImageAnalyzer(imageAnalyzer: ImageAnalyzer?)

  fun setImageAnalyzerEnabled(isImageAnalyzerEnabled: Boolean)

  fun setFrameRate(
    minFps: Int,
    maxFps: Int = minFps,
  )

  fun setPinchToZoomEnabled(isPinchToZoomEnabled: Boolean)

  fun setOrientationStrategy(orientationStrategy: OrientationStrategy)

  fun setFlashMode(flashMode: FlashMode)

  fun setTorchEnabled(isTorchEnabled: Boolean)

  fun setExposureCompensation(exposureCompensation: Float)

  fun setFocusOnTapEnabled(isFocusOnTapEnabled: Boolean)

  fun setImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy)

  fun isVideoStabilizationSupported(videoStabilizationMode: VideoStabilizationMode): Boolean

  fun setVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode)

  fun setZoomRatio(zoomRatio: Float)

  fun resetConfig()
}
