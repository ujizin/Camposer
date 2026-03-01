package com.ujizin.camposer.internal.core

import com.ujizin.camposer.internal.capture.JvmCameraCapture
import org.bytedeco.opencv.opencv_core.Mat

internal interface JvmCameraEngine : CameraEngine {
  val capture: JvmCameraCapture

  /** The latest frame from the camera, null until first frame is read. */
  var currentMat: Mat?
}
