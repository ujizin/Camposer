package com.ujizin.camposer.internal.capture

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

  override fun set(
    propId: Int,
    value: Double,
  ): Boolean = true

  override fun get(propId: Int): Double =
    when (propId) {
      CAP_PROP_ZOOM -> 10.0
      CAP_PROP_EXPOSURE -> -3.0
      CAP_PROP_FPS -> 30.0
      else -> 0.0
    }

  override fun release() {}

  override fun startStreaming() {}

  override suspend fun stopStreaming() {}

  private val _currentFrame = MutableStateFlow<ImageBitmap?>(null)
  override val currentFrame: StateFlow<ImageBitmap?> = _currentFrame

  private val _isStreaming = MutableStateFlow(false)
  override val isStreaming: StateFlow<Boolean> = _isStreaming

  override var currentMat: Mat? = null

  override fun addFrameListener(listener: (Mat) -> Unit) {}

  override fun removeFrameListener(listener: (Mat) -> Unit) {}
}
