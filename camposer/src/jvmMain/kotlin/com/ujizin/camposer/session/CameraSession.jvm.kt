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
import com.ujizin.camposer.internal.utils.Logger
import com.ujizin.camposer.state.CameraState
import kotlinx.coroutines.flow.StateFlow

@Stable
public actual class CameraSession internal constructor(
  internal actual val cameraEngine: CameraEngine,
  public actual val controller: CameraController = cameraEngine.cameraController,
  public actual val info: CameraInfo = cameraEngine.cameraInfo,
  public actual val state: CameraState = cameraEngine.cameraState,
  private val recordControllerFactory: (CameraEngine) -> DefaultRecordController = { engine ->
    DefaultRecordController(engine)
  },
) {
  public actual var isInitialized: Boolean by mutableStateOf(false)
    private set

  public actual var hasInitializationError: Boolean by mutableStateOf(false)
    internal set

  public actual var isStreaming: Boolean by mutableStateOf(false)
    internal set

  private var recordController: DefaultRecordController? = null

  private val jvmCapture: JvmCameraCapture
    get() = (cameraEngine as JvmCameraEngine).capture

  public val currentFrame: StateFlow<ImageBitmap?>
    get() = jvmCapture.currentFrame

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
      val capture = jvmCapture
      val success = capture.open(state.camSelector.value.deviceIndex)
      if (!success) {
        error("Failed to open camera device at index ${state.camSelector.value.deviceIndex}")
      }

      val rc = recordControllerFactory(cameraEngine)
      recordController = rc
      controller.initialize(
        recordController = rc,
        takePictureCommand = DefaultTakePictureCommand(cameraEngine),
        cameraEngine = cameraEngine,
      )

      isInitialized = true
      capture.startStreaming()
    }.onFailure { error ->
      isInitialized = false
      hasInitializationError = true
      Logger.error("Failed to initialize camera session", error)
    }

  public actual fun retryInitialization() {
    if (!hasInitializationError) return

    recordController?.dispose()
    recordController = null
    hasInitializationError = false
    isInitialized = false

    setupCamera()
  }

  internal actual fun onSessionStarted() {
    if (controller.isRunning.value) return
    controller.onSessionStarted()
  }

  internal fun dispose() {
    isStreaming = false
    jvmCapture.release()
    state.dispose()
    controller.dispose()
    recordController?.dispose()
    (jvmCapture as? JvmCameraCaptureImpl)?.dispose()
  }
}
