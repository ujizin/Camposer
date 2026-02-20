package com.ujizin.camposer.session

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LifecycleOwner
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.controller.takepicture.DefaultTakePictureCommand
import com.ujizin.camposer.info.AndroidCameraInfo
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.AndroidCameraEngine
import com.ujizin.camposer.internal.core.CameraEngine
import com.ujizin.camposer.internal.core.CameraEngineImpl
import com.ujizin.camposer.internal.core.camerax.CameraXController
import com.ujizin.camposer.internal.core.camerax.CameraXControllerWrapper
import com.ujizin.camposer.internal.utils.Logger
import com.ujizin.camposer.state.CameraState

/**
 * A state object that can be hoisted to control camera, take picture or record video.
 *
 * To be created use [rememberCameraSession].
 * */
@Stable
public actual class CameraSession internal constructor(
  internal actual val cameraEngine: CameraEngine,
  public actual val controller: CameraController = cameraEngine.cameraController,
  public actual val info: CameraInfo = cameraEngine.cameraInfo,
  public actual val state: CameraState = cameraEngine.cameraState,
) {
  internal val androidCameraEngine
    get() = cameraEngine as AndroidCameraEngine

  internal val cameraXControllerWrapper: CameraXController
    get() = androidCameraEngine.cameraXController

  @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public val cameraXController: LifecycleCameraController
    get() = cameraXControllerWrapper.get()

  public var previewView: PreviewView? = null

  /**
   * Check if camera is streaming or not.
   * */
  public actual var isStreaming: Boolean by mutableStateOf(false)
    internal set

  /**
   * Check if camera state is initialized or not.
   * */
  public actual var isInitialized: Boolean by mutableStateOf(false)
    internal set

  public actual var hasInitializationError: Boolean by mutableStateOf(false)
    internal set

  public constructor(
    context: Context,
    cameraController: CameraController,
  ) : this(
    context = context,
    cameraController = cameraController,
    cameraXController = CameraXControllerWrapper(context),
  )

  internal constructor(
    context: Context,
    cameraController: CameraController,
    cameraXController: CameraXController,
  ) : this(
    context = context,
    cameraController = cameraController,
    cameraXController = cameraXController,
    info = CameraInfo(
      cameraInfo = AndroidCameraInfo(cameraXController),
    ),
  )

  internal constructor(
    context: Context,
    cameraController: CameraController,
    cameraXController: CameraXController,
    info: CameraInfo,
  ) : this(
    controller = cameraController,
    cameraEngine = CameraEngineImpl(
      cameraXController = cameraXController,
      cameraInfo = info,
      cameraController = cameraController,
    ),
    info = info,
  )

  init {
    initialize()
  }

  private fun initialize() {
    runCatching {
      controller.initialize(
        recordController = DefaultRecordController(cameraEngine = cameraEngine),
        takePictureCommand = DefaultTakePictureCommand(cameraEngine = cameraEngine),
        cameraEngine = cameraEngine,
        cameraState = state,
        cameraInfo = info,
      )

      cameraXControllerWrapper.onInitialize {
        info.rebind()
        androidCameraEngine.onCameraInitialized()
        isInitialized = true
      }
    }.onFailure { error ->
      isInitialized = false
      hasInitializationError = true
      Logger.error("Failed to initialize camera session", error)
    }
  }

  public actual fun retryInitialization(): Boolean {
    if (!hasInitializationError) {
      return isInitialized
    }

    hasInitializationError = false
    isInitialized = false

    initialize()
    return isInitialized
  }

  internal actual fun onSessionStarted() {
    cameraXControllerWrapper.mainExecutor.execute {
      controller.onSessionStarted()
    }
  }

  /**
   * This is unusual to make in camerax controller, however to update the preview view or implementation mode, this needs to be made
   * */
  internal fun rebind(lifecycle: LifecycleOwner) {
    cameraXControllerWrapper.unbind()
    cameraXControllerWrapper.bindToLifecycle(lifecycle)
  }

  internal fun dispose() {
    state.dispose()
    cameraXControllerWrapper.unbind()
  }
}
