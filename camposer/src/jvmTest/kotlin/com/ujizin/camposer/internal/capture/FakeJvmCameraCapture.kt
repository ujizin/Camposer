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
  private val setCallCounts = mutableMapOf<Int, Int>()
  private val lastSetValues = mutableMapOf<Int, Double>()
  private val frameListeners = mutableSetOf<(Mat) -> Unit>()

  internal var openCalls: Int = 0
    private set

  internal var releaseCalls: Int = 0
    private set

  internal var startStreamingCalls: Int = 0
    private set

  internal var stopStreamingCalls: Int = 0
    private set

  internal var addFrameListenerCalls: Int = 0
    private set

  internal var removeFrameListenerCalls: Int = 0
    private set

  override fun open(deviceIndex: Int): Boolean {
    openCalls++
    return openResult
  }

  override val isOpen: Boolean get() = openResult

  override fun read(mat: Mat): Boolean = openResult

  override fun set(
    propId: Int,
    value: Double,
  ): Boolean {
    setCallCounts[propId] = (setCallCounts[propId] ?: 0) + 1
    lastSetValues[propId] = value
    return true
  }

  override fun get(propId: Int): Double =
    when (propId) {
      CAP_PROP_ZOOM -> 10.0
      CAP_PROP_EXPOSURE -> -3.0
      CAP_PROP_FPS -> 30.0
      else -> 0.0
    }

  override fun release() {
    releaseCalls++
  }

  override fun startStreaming() {
    startStreamingCalls++
  }

  override suspend fun stopStreaming() {
    stopStreamingCalls++
  }

  private val _currentFrame = MutableStateFlow<ImageBitmap?>(null)
  override val currentFrame: StateFlow<ImageBitmap?> = _currentFrame

  private val _isStreaming = MutableStateFlow(false)
  override val isStreaming: StateFlow<Boolean> = _isStreaming

  override var currentMat: Mat? = null

  override fun addFrameListener(listener: (Mat) -> Unit) {
    addFrameListenerCalls++
    frameListeners += listener
  }

  override fun removeFrameListener(listener: (Mat) -> Unit) {
    removeFrameListenerCalls++
    frameListeners -= listener
  }

  internal fun setCallCount(propId: Int): Int = setCallCounts[propId] ?: 0

  internal fun lastSetValue(propId: Int): Double? = lastSetValues[propId]

  internal fun frameListenerCount(): Int = frameListeners.size
}
