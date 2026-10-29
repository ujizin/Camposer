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
import com.ujizin.camposer.internal.core.CameraEngine
import com.ujizin.camposer.internal.core.CameraEngineImpl
import com.ujizin.camposer.internal.core.JvmCameraEngine
import com.ujizin.camposer.internal.core.jvm.DefaultJvmCameraController
import com.ujizin.camposer.internal.core.jvm.JvmCameraController
import com.ujizin.camposer.internal.preview.PreviewGeometry
import com.ujizin.camposer.internal.utils.Logger
import com.ujizin.camposer.state.CameraState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * A state object that can be hoisted to control camera, take picture or record video.
 *
 * To be created use [rememberCameraSession].
 * */
@Stable
public actual class CameraSession internal constructor(
  internal actual val cameraEngine: CameraEngine,
  internal val jvmCameraController: JvmCameraController =
    (cameraEngine as JvmCameraEngine).jvmCameraController,
  public actual val controller: CameraController = cameraEngine.cameraController,
  public actual val info: CameraInfo = cameraEngine.cameraInfo,
  public actual val state: CameraState = cameraEngine.cameraState,
  sessionDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
  private val jvmCameraEngine: JvmCameraEngine
    get() = cameraEngine as JvmCameraEngine

  private val sessionScope = CoroutineScope(SupervisorJob() + sessionDispatcher)

  public actual var isInitialized: Boolean by mutableStateOf(false)
    private set

  public actual var hasInitializationError: Boolean by mutableStateOf(false)
    internal set

  public actual var isStreaming: Boolean by mutableStateOf(false)
    internal set

  /** Latest preview frame, null until the first frame arrives. */
  internal val currentFrame: StateFlow<ImageBitmap?>
    get() = jvmCameraController.currentFrame

  public constructor(controller: CameraController) : this(
    controller = controller,
    jvmCameraController = DefaultJvmCameraController(),
  )

  internal constructor(
    controller: CameraController,
    jvmCameraController: JvmCameraController,
  ) : this(
    cameraEngine = CameraEngineImpl(
      cameraController = controller,
      cameraInfo = CameraInfo(jvmCameraController),
      jvmCameraController = jvmCameraController,
    ),
  )

  init {
    jvmCameraController.isStreaming
      .onEach { isStreaming = it }
      .launchIn(sessionScope)
    setupCamera()
  }

  private fun setupCamera() {
    runCatching {
      controller.initialize(
        recordController = DefaultRecordController(cameraEngine),
        takePictureCommand = DefaultTakePictureCommand(cameraEngine),
        cameraEngine = cameraEngine,
      )
    }.onFailure { error ->
      onInitializationFailed(error)
      return
    }

    jvmCameraController.open(state.camSelector.value.deviceIndex) { result ->
      result
        .onSuccess {
          info.updateInfo()
          jvmCameraEngine.onCameraInitialized()
          isInitialized = true
        }.onFailure(::onInitializationFailed)
    }
  }

  private fun onInitializationFailed(error: Throwable) {
    isInitialized = false
    hasInitializationError = true
    Logger.error("Failed to initialize camera session", error)
  }

  public actual fun retryInitialization() {
    if (!hasInitializationError) {
      return
    }

    hasInitializationError = false
    isInitialized = false
    setupCamera()
  }

  internal actual fun onSessionStarted() {
    if (controller.isRunning.value) return
    controller.onSessionStarted()
  }

  internal fun startCamera() {
    jvmCameraController.startStreaming()
  }

  internal fun stopCamera() {
    jvmCameraController.stopStreaming()
  }

  internal fun updatePreviewGeometry(geometry: PreviewGeometry?) {
    jvmCameraController.updatePreviewGeometry(geometry)
  }

  internal fun dispose() {
    sessionScope.cancel()
    state.dispose()
    controller.dispose()
    jvmCameraController.release()
  }
}
