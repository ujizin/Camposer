package com.ujizin.camposer.controller.record

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.internal.core.CameraEngine
import com.ujizin.camposer.internal.core.JvmCameraEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal actual class DefaultRecordController(
  private val cameraEngine: CameraEngine,
) : RecordController {

  private val _isMuted = MutableStateFlow(false)
  actual override val isMuted: StateFlow<Boolean> get() = _isMuted

  private val _isRecording = MutableStateFlow(false)
  actual override val isRecording: StateFlow<Boolean> get() = _isRecording

  private var pendingFilename: String? = null
  private var pendingCallback: ((CaptureResult<String>) -> Unit)? = null

  actual override fun startRecording(
    filename: String,
    onVideoCaptured: (CaptureResult<String>) -> Unit,
  ) {
    pendingFilename = filename
    pendingCallback = onVideoCaptured
    _isRecording.update { true }
  }

  actual override fun resumeRecording(): Result<Boolean> = Result.success(true)

  actual override fun pauseRecording(): Result<Boolean> = Result.success(true)

  actual override fun stopRecording(): Result<Boolean> {
    val filename = pendingFilename ?: return Result.failure(IllegalStateException("Not recording"))
    val callback = pendingCallback ?: return Result.failure(IllegalStateException("Not recording"))

    _isRecording.update { false }
    _isMuted.update { false }
    pendingFilename = null
    pendingCallback = null

    val hasError = (cameraEngine as? JvmCameraEngine)?.hasRecordingError ?: false
    if (hasError) {
      callback(CaptureResult.Error(Exception("Recording error")))
    } else {
      callback(CaptureResult.Success(filename))
    }

    return Result.success(true)
  }

  actual override fun muteRecording(isMuted: Boolean): Result<Boolean> {
    _isMuted.update { isMuted }
    return Result.success(true)
  }
}
