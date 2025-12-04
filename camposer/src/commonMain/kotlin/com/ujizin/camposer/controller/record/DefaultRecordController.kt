package com.ujizin.camposer.controller.record

import com.ujizin.camposer.CaptureResult

internal expect class DefaultRecordController : RecordController {
  override var isMuted: Boolean
    internal set

  override var isRecording: Boolean
    internal set

  override fun startRecording(
    filename: String,
    onVideoCaptured: (CaptureResult<String>) -> Unit,
  )

  override fun resumeRecording(): Result<Boolean>

  override fun pauseRecording(): Result<Boolean>

  override fun stopRecording(): Result<Boolean>

  override fun muteRecording(isMuted: Boolean): Result<Boolean>
}
