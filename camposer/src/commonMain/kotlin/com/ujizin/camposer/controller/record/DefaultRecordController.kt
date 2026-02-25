package com.ujizin.camposer.controller.record

import com.ujizin.camposer.CaptureResult
import kotlinx.coroutines.flow.StateFlow

internal expect class DefaultRecordController : RecordController {
  override val isMuted: StateFlow<Boolean>
  override val isRecording: StateFlow<Boolean>

  override fun startRecording(
    filename: String,
    onVideoCaptured: (CaptureResult<String>) -> Unit,
  )

  override fun resumeRecording(): Result<Boolean>

  override fun pauseRecording(): Result<Boolean>

  override fun stopRecording(): Result<Boolean>

  override fun muteRecording(isMuted: Boolean): Result<Boolean>
}
