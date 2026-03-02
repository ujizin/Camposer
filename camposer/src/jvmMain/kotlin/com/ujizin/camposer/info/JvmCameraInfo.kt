package com.ujizin.camposer.info

import com.ujizin.camposer.internal.capture.JvmCameraCapture
import com.ujizin.camposer.internal.capture.JvmCameraCaptureImpl
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_EXPOSURE
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FPS
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_ZOOM

internal open class JvmCameraInfo(
  private val capture: JvmCameraCapture = JvmCameraCaptureImpl(),
) {
  /** Desktop cameras do not have a flash unit. Override in tests/subclasses as needed. */
  open val isFlashSupported: Boolean = false

  open val isExposureSupported: Boolean
    get() = capture.isOpen && capture.get(CAP_PROP_EXPOSURE) != 0.0

  open val isZoomSupported: Boolean
    get() = capture.isOpen && capture.get(CAP_PROP_ZOOM) > 0.0

  open val minZoom: Float get() = 1f
  open val maxZoom: Float get() = if (isZoomSupported) capture.get(CAP_PROP_ZOOM).toFloat() else 1f

  open val minExposure: Float = 0f
  open val maxExposure: Float = 0f

  open val minFPS: Int get() = 0
  open val maxFPS: Int get() = if (capture.isOpen) capture.get(CAP_PROP_FPS).toInt() else 30
}
