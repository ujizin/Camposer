package com.ujizin.camposer.controller.record

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.error.CaptureModeException
import com.ujizin.camposer.internal.core.CameraManagerInternal
import com.ujizin.camposer.internal.core.IOSCameraManagerInternal
import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.internal.extensions.toCaptureResult
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode

internal actual class DefaultRecordController private constructor(
  private val cameraManager: IOSCameraManagerInternal,
) : RecordController {
  internal constructor(cameraControllerInternal: CameraManagerInternal) : this(
    cameraControllerInternal as IOSCameraManagerInternal,
  )

  private val controller: IOSCameraController
    get() = cameraManager.cameraController

  private val cameraState: CameraState
    get() = cameraManager.cameraState

  actual override var isMuted: Boolean = controller.isMuted
    get() = controller.isMuted
    private set

  actual override var isRecording: Boolean = controller.isRecording
    get() = controller.isRecording
    private set

  actual override fun startRecording(
    filename: String,
    onVideoCaptured: (CaptureResult<String>) -> Unit,
  ) = controller.startRecording(
    isMirrorEnabled = this@DefaultRecordController.cameraManager.isMirrorEnabled(),
    videoOrientation = this@DefaultRecordController.cameraManager.getCurrentVideoOrientation(),
    filename = filename,
    onVideoCaptured = { result -> onVideoCaptured(result.toCaptureResult()) },
  )

  actual override fun resumeRecording(): Result<Boolean> {
    if (cameraState.captureMode != CaptureMode.Video) {
      return Result.failure(CaptureModeException(CaptureMode.Video))
    }

    return controller.resumeRecording()
  }

  actual override fun pauseRecording(): Result<Boolean> {
    if (cameraState.captureMode != CaptureMode.Video) {
      return Result.failure(CaptureModeException(CaptureMode.Video))
    }

    return controller.pauseRecording()
  }

  actual override fun stopRecording(): Result<Boolean> {
    if (cameraState.captureMode != CaptureMode.Video) {
      return Result.failure(CaptureModeException(CaptureMode.Video))
    }

    return controller.stopRecording()
  }

  actual override fun muteRecording(isMuted: Boolean): Result<Boolean> =
    controller.muteRecording(isMuted)
}
