package com.ujizin.camposer.controller.record

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.internal.core.CameraEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal actual class DefaultRecordController(
  @Suppress("UNUSED_PARAMETER") private val cameraEngine: CameraEngine,
) : RecordController {

  private val _isMuted = MutableStateFlow(false)
  actual override val isMuted: StateFlow<Boolean> get() = _isMuted

  private val _isRecording = MutableStateFlow(false)
  actual override val isRecording: StateFlow<Boolean> get() = _isRecording

  actual override fun startRecording(
    filename: String,
    onVideoCaptured: (CaptureResult<String>) -> Unit,
  ) {
    // TODO: Video recording not supported on JVM desktop (future work via FFmpeg)
    onVideoCaptured(
      CaptureResult.Error(
        UnsupportedOperationException("Video recording is not supported on JVM desktop"),
      ),
    )
  }

  actual override fun resumeRecording(): Result<Boolean> {
    // TODO: Video recording not supported on JVM desktop
    return Result.failure(
      UnsupportedOperationException("Video recording is not supported on JVM desktop"),
    )
  }

  actual override fun pauseRecording(): Result<Boolean> {
    // TODO: Video recording not supported on JVM desktop
    return Result.failure(
      UnsupportedOperationException("Video recording is not supported on JVM desktop"),
    )
  }

  actual override fun stopRecording(): Result<Boolean> {
    // TODO: Video recording not supported on JVM desktop
    return Result.failure(
      UnsupportedOperationException("Video recording is not supported on JVM desktop"),
    )
  }

  actual override fun muteRecording(isMuted: Boolean): Result<Boolean> {
    // TODO: Video recording not supported on JVM desktop
    return Result.failure(
      UnsupportedOperationException("Video recording is not supported on JVM desktop"),
    )
  }
}
