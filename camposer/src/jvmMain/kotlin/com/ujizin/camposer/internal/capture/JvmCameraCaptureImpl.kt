package com.ujizin.camposer.internal.capture

import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_videoio.VideoCapture

internal class JvmCameraCaptureImpl : JvmCameraCapture {
  private val capture = VideoCapture()

  override fun open(deviceIndex: Int): Boolean = capture.open(deviceIndex)

  override val isOpen: Boolean get() = capture.isOpened

  override fun read(mat: Mat): Boolean = capture.read(mat)

  override fun set(
    propId: Int,
    value: Double,
  ): Boolean = capture.set(propId, value)

  override fun get(propId: Int): Double = capture.get(propId)

  override fun release() {
    if (capture.isOpened) capture.release()
  }
}
