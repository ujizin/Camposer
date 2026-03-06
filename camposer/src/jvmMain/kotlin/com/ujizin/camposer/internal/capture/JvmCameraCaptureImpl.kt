package com.ujizin.camposer.internal.capture

import androidx.compose.ui.graphics.ImageBitmap
import com.ujizin.camposer.internal.extensions.toImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_videoio.VideoCapture

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
internal class JvmCameraCaptureImpl : JvmCameraCapture {
  private val capture = VideoCapture()
  private val cameraDispatcher = newSingleThreadContext("CameraIO")
  private val streamingScope = CoroutineScope(cameraDispatcher + SupervisorJob())
  private var frameLoopJob: Job? = null

  private val _currentFrame = MutableStateFlow<ImageBitmap?>(null)
  override val currentFrame: StateFlow<ImageBitmap?> = _currentFrame.asStateFlow()

  private val _isStreaming = MutableStateFlow(false)
  override val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

  override var currentMat: Mat? = null

  private val frameListeners = mutableListOf<(Mat) -> Unit>()

  override fun addFrameListener(listener: (Mat) -> Unit) {
    frameListeners.add(listener)
  }

  override fun removeFrameListener(listener: (Mat) -> Unit) {
    frameListeners.remove(listener)
  }

  override fun open(deviceIndex: Int): Boolean = capture.open(deviceIndex)

  override val isOpen: Boolean get() = capture.isOpened

  override fun read(mat: Mat): Boolean = capture.read(mat)

  override fun set(
    propId: Int,
    value: Double,
  ): Boolean = capture.set(propId, value)

  override fun get(propId: Int): Double = capture.get(propId)

  override fun startStreaming() {
    frameLoopJob = streamingScope.launch {
      _isStreaming.value = true
      val mat = Mat()
      try {
        while (isActive) {
          val read = capture.read(mat)
          if (!read || mat.empty()) {
            delay(16)
            continue
          }

          currentMat = mat.clone()

          val bitmap = mat.toImageBitmap()
          _currentFrame.update { bitmap }

          frameListeners.forEach { it.invoke(mat) }
        }
      } finally {
        _isStreaming.value = false
      }
    }
  }

  override suspend fun stopStreaming() {
    val job = frameLoopJob ?: return
    frameLoopJob = null
    job.cancelAndJoin()
  }

  override fun release() {
    frameLoopJob?.cancel()
    frameLoopJob = null
    if (capture.isOpened) capture.release()
  }

  fun dispose() {
    release()
    streamingScope.cancel()
    cameraDispatcher.close()
  }
}
