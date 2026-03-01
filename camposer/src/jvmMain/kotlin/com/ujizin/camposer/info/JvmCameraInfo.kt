package com.ujizin.camposer.info

import com.ujizin.camposer.internal.capture.JvmCameraCapture
import com.ujizin.camposer.internal.capture.JvmCameraCaptureImpl
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_EXPOSURE
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FPS
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_ZOOM

internal class JvmCameraInfo(
  private val capture: JvmCameraCapture = JvmCameraCaptureImpl(),
) {
  val isExposureSupported: Boolean
    get() = capture.isOpen && capture.get(CAP_PROP_EXPOSURE) != 0.0

  val isZoomSupported: Boolean
    get() = capture.isOpen && capture.get(CAP_PROP_ZOOM) > 0.0

  val minZoom: Float get() = 1f
  val maxZoom: Float get() = if (isZoomSupported) capture.get(CAP_PROP_ZOOM).toFloat() else 1f

  val minFPS: Int get() = 0
  val maxFPS: Int get() = if (capture.isOpen) capture.get(CAP_PROP_FPS).toInt() else 30
}
