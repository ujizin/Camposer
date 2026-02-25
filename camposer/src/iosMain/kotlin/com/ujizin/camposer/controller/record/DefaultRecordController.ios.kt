package com.ujizin.camposer.controller.record

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.error.CaptureModeException
import com.ujizin.camposer.internal.core.CameraEngine
import com.ujizin.camposer.internal.core.IOSCameraEngine
import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.internal.extensions.toCaptureResult
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode
import kotlinx.coroutines.flow.StateFlow

internal actual class DefaultRecordController private constructor(
  private val cameraEngine: IOSCameraEngine,
) : RecordController {
  internal constructor(cameraControllerInternal: CameraEngine) : this(
    cameraControllerInternal as IOSCameraEngine,
  )

  private val controller: IOSCameraController
    get() = cameraEngine.iOSCameraController

  private val cameraState: CameraState
    get() = cameraEngine.cameraState

  actual override val isMuted: StateFlow<Boolean>
    get() = controller.isMuted

  actual override val isRecording: StateFlow<Boolean>
    get() = controller.isRecording

  actual override fun startRecording(
    filename: String,
    onVideoCaptured: (CaptureResult<String>) -> Unit,
  ) = controller.startRecording(
    isMirrorEnabled = this@DefaultRecordController.cameraEngine.isMirrorEnabled(),
    videoOrientation = this@DefaultRecordController.cameraEngine.getCurrentVideoOrientation(),
    filename = filename,
    onVideoCaptured = { result -> onVideoCaptured(result.toCaptureResult()) },
  )

  actual override fun resumeRecording(): Result<Boolean> {
    if (cameraState.captureMode.value != CaptureMode.Video) {
      return Result.failure(CaptureModeException(CaptureMode.Video))
    }

    return controller.resumeRecording()
  }

  actual override fun pauseRecording(): Result<Boolean> {
    if (cameraState.captureMode.value != CaptureMode.Video) {
      return Result.failure(CaptureModeException(CaptureMode.Video))
    }

    return controller.pauseRecording()
  }

  actual override fun stopRecording(): Result<Boolean> {
    if (cameraState.captureMode.value != CaptureMode.Video) {
      return Result.failure(CaptureModeException(CaptureMode.Video))
    }

    return controller.stopRecording()
  }

  actual override fun muteRecording(isMuted: Boolean): Result<Boolean> =
    controller.muteRecording(isMuted)
}
