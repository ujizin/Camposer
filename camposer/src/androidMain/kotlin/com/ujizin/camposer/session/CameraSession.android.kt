package com.ujizin.camposer.session

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
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
import com.ujizin.camposer.extensions.compatMainExecutor
import com.ujizin.camposer.info.AndroidCameraInfo
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.AndroidCameraManagerInternal
import com.ujizin.camposer.internal.core.CameraManagerInternal
import com.ujizin.camposer.internal.core.CameraManagerInternalImpl
import com.ujizin.camposer.internal.core.camerax.CameraXController
import com.ujizin.camposer.internal.core.camerax.CameraXControllerWrapper
import com.ujizin.camposer.state.CameraState

/**
 * A state object that can be hoisted to control camera, take picture or record video.
 *
 * To be created use [rememberCameraSession].
 * */
@Stable
public actual class CameraSession private constructor(
  internal actual val cameraManager: CameraManagerInternal,
  public actual val controller: CameraController,
  public actual val info: CameraInfo,
  public actual val state: CameraState = cameraManager.cameraState,
) {
  internal val androidCameraManagerInternal = cameraManager as AndroidCameraManagerInternal
  internal val cameraXControllerWrapper: CameraXController
    get() = androidCameraManagerInternal.controller

  @get:RestrictTo(RestrictTo.Scope.LIBRARY)
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
      mainExecutor = context.compatMainExecutor,
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
    cameraManager = CameraManagerInternalImpl(
      controller = cameraXController,
      cameraInfo = info,
    ),
    info = info,
  ) {
    this.previewView = PreviewView(context)
  }

  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  internal constructor(
    cameraController: CameraController,
    cameraManagerInternal: CameraManagerInternal,
    cameraInfo: CameraInfo,
  ) : this(
    controller = cameraController,
    cameraManager = cameraManagerInternal,
    state = cameraManagerInternal.cameraState,
    info = cameraInfo,
  )

  init {
    controller.initialize(
      recordController = DefaultRecordController(cameraManager = cameraManager),
      takePictureCommand = DefaultTakePictureCommand(cameraManager = cameraManager),
      cameraState = state,
      cameraInfo = info,
    )

    cameraXControllerWrapper.onInitialize {
      info.rebind()
      cameraXControllerWrapper.isPinchToZoomEnabled = false
      androidCameraManagerInternal.onCameraInitialized()
      isInitialized = true
    }
  }

  internal actual fun onSessionStarted() {
    controller.onSessionStarted()
  }

  /**
   * This is unusual to make in camerax controller, however to update the preview view or implementation mode, this needs to be made
   * */
  internal fun rebind(lifecycle: LifecycleOwner) {
    cameraXControllerWrapper.unbind()
    cameraXControllerWrapper.bindToLifecycle(lifecycle)
  }

  internal fun dispose() {
    cameraXControllerWrapper.unbind()
  }
}
