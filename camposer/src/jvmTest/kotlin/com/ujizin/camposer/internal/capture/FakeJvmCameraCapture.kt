package com.ujizin.camposer.internal.capture

import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_EXPOSURE
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FPS
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_ZOOM
import org.bytedeco.opencv.opencv_core.Mat

internal class FakeJvmCameraCapture(
  private val openResult: Boolean = true,
) : JvmCameraCapture {
  override fun open(deviceIndex: Int): Boolean = openResult
  override val isOpen: Boolean get() = openResult
  override fun read(mat: Mat): Boolean = openResult
  override fun set(propId: Int, value: Double): Boolean = true
  override fun get(propId: Int): Double = when (propId) {
    CAP_PROP_ZOOM -> 10.0       // simulate 10x optical zoom range
    CAP_PROP_EXPOSURE -> -3.0   // simulate supported exposure compensation
    CAP_PROP_FPS -> 30.0
    else -> 0.0
  }
  override fun release() {}
}
