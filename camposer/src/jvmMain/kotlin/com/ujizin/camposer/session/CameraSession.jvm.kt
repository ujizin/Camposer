package com.ujizin.camposer.session

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.controller.takepicture.DefaultTakePictureCommand
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.info.JvmCameraInfo
import com.ujizin.camposer.internal.capture.JvmCameraCapture
import com.ujizin.camposer.internal.capture.JvmCameraCaptureImpl
import com.ujizin.camposer.internal.core.CameraEngine
import com.ujizin.camposer.internal.core.CameraEngineImpl
import com.ujizin.camposer.internal.core.JvmCameraEngine
import com.ujizin.camposer.internal.extensions.toImageBitmap
import com.ujizin.camposer.internal.utils.Logger
import com.ujizin.camposer.state.CameraState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import org.bytedeco.opencv.global.opencv_core
import org.bytedeco.opencv.opencv_core.Mat

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
@Stable
public actual class CameraSession internal constructor(
  internal actual val cameraEngine: CameraEngine,
  public actual val controller: CameraController = cameraEngine.cameraController,
  public actual val info: CameraInfo = cameraEngine.cameraInfo,
  public actual val state: CameraState = cameraEngine.cameraState,
) {
  public actual var isInitialized: Boolean by mutableStateOf(false)
    private set

  public actual var hasInitializationError: Boolean by mutableStateOf(false)
    internal set

  public actual var isStreaming: Boolean by mutableStateOf(false)
    internal set

  private val cameraDispatcher = newSingleThreadContext("CameraIO")
  private val sessionScope = CoroutineScope(cameraDispatcher + SupervisorJob())

  private val _currentFrame = MutableStateFlow<ImageBitmap?>(null)
  public val currentFrame: StateFlow<ImageBitmap?> = _currentFrame.asStateFlow()

  private var frameLoopJob: Job? = null

  public constructor(controller: CameraController) : this(
    controller = controller,
    capture = JvmCameraCaptureImpl(),
  )

  internal constructor(
    controller: CameraController,
    capture: JvmCameraCapture,
  ) : this(
    cameraEngine = CameraEngineImpl(
      cameraController = controller,
      cameraInfo = CameraInfo(JvmCameraInfo(capture)),
      capture = capture,
    ),
  )

  init {
    setupCamera()
  }

  private fun setupCamera() =
    runCatching {
      val jvmEngine = cameraEngine as JvmCameraEngine
      val success = jvmEngine.capture.open(state.camSelector.value.deviceIndex)
      if (!success) {
        error("Failed to open camera device at index ${state.camSelector.value.deviceIndex}")
      }

      controller.initialize(
        recordController = DefaultRecordController(cameraEngine),
        takePictureCommand = DefaultTakePictureCommand(cameraEngine),
        cameraEngine = cameraEngine,
      )

      isInitialized = true
      startFrameLoop(jvmEngine)
    }.onFailure { error ->
      isInitialized = false
      hasInitializationError = true
      Logger.error("Failed to initialize camera session", error)
    }

  private fun startFrameLoop(jvmEngine: JvmCameraEngine) {
    frameLoopJob = sessionScope.launch {
      isStreaming = true
      val mat = Mat()
      try {
        while (isActive) {
          val read = jvmEngine.capture.read(mat)
          if (!read || mat.empty()) {
            delay(16)
            continue
          }

          jvmEngine.currentMat = mat.clone()

          val frameMat = if (cameraEngine.isMirrorEnabled()) {
            val flipped = Mat()
            opencv_core.flip(mat, flipped, 1)
            flipped
          } else {
            mat
          }

          val bitmap = frameMat.toImageBitmap()
          _currentFrame.update { bitmap }

          if (state.isImageAnalyzerEnabled.value) {
            state.imageAnalyzer.value?.analyze(mat)
          }
        }
      } finally {
        isStreaming = false
      }
    }
  }

  public actual fun retryInitialization() {
    if (!hasInitializationError) return

    hasInitializationError = false
    isInitialized = false
    frameLoopJob?.cancel()
    frameLoopJob = null

    setupCamera()
  }

  internal actual fun onSessionStarted() {
    if (controller.isRunning.value) return
    controller.onSessionStarted()
  }

  internal fun dispose() {
    isStreaming = false
    frameLoopJob?.cancel()
    frameLoopJob = null
    val jvmEngine = cameraEngine as? JvmCameraEngine
    jvmEngine?.capture?.release()
    state.dispose()
    controller.dispose()
    sessionScope.cancel()
    cameraDispatcher.close()
  }
}
