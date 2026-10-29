package com.ujizin.camposer.controller.record

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.error.CaptureModeException
import com.ujizin.camposer.internal.core.CameraEngine
import com.ujizin.camposer.internal.core.JvmCameraEngine
import com.ujizin.camposer.internal.core.jvm.JvmCameraController
import com.ujizin.camposer.internal.extensions.toCaptureResult
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode
import kotlinx.coroutines.flow.StateFlow

internal actual class DefaultRecordController private constructor(
  private val cameraEngine: JvmCameraEngine,
) : RecordController {
  internal constructor(cameraEngine: CameraEngine) : this(cameraEngine as JvmCameraEngine)

  private val controller: JvmCameraController
    get() = cameraEngine.jvmCameraController

  private val cameraState: CameraState
    get() = cameraEngine.cameraState

  actual override val isMuted: StateFlow<Boolean>
    get() = controller.isMuted

  actual override val isRecording: StateFlow<Boolean>
    get() = controller.isRecording

  actual override fun startRecording(
    filename: String,
    onVideoCaptured: (CaptureResult<String>) -> Unit,
  ) {
    if (cameraState.captureMode.value != CaptureMode.Video) {
      onVideoCaptured(CaptureResult.Error(CaptureModeException(CaptureMode.Video)))
      return
    }

    controller.startRecording(
      filename = filename,
      isMirrored = cameraEngine.isMirrorEnabled(),
      onVideoCaptured = { result -> onVideoCaptured(result.toCaptureResult()) },
    )
  }

  actual override fun resumeRecording(): Result<Boolean> =
    requireVideoMode { controller.resumeRecording() }

  actual override fun pauseRecording(): Result<Boolean> =
    requireVideoMode { controller.pauseRecording() }

  actual override fun stopRecording(): Result<Boolean> =
    requireVideoMode { controller.stopRecording() }

  actual override fun muteRecording(isMuted: Boolean): Result<Boolean> =
    controller.muteRecording(isMuted)

  private inline fun requireVideoMode(block: () -> Result<Boolean>): Result<Boolean> {
    if (cameraState.captureMode.value != CaptureMode.Video) {
      return Result.failure(CaptureModeException(CaptureMode.Video))
    }
    return block()
  }
}
