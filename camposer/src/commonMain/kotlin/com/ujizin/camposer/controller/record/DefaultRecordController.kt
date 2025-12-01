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

  override fun resumeRecording()

  override fun pauseRecording()

  override fun stopRecording()

  override fun muteRecording(isMuted: Boolean)
}
