package com.ujizin.camposer.internal.capture

import org.bytedeco.opencv.opencv_core.Mat

internal class FakeJvmCameraCapture(
  private val openResult: Boolean = true,
) : JvmCameraCapture {
  override fun open(deviceIndex: Int): Boolean = openResult
  override val isOpen: Boolean get() = openResult
  override fun read(mat: Mat): Boolean = openResult
  override fun set(propId: Int, value: Double): Boolean = true
  override fun get(propId: Int): Double = 0.0
  override fun release() {}
}
