package com.ujizin.camposer.controller.record

import com.ujizin.camposer.result.CaptureResult
import kotlinx.io.files.Path

internal interface RecordController {

    val isRecording: Boolean

    fun startRecording(path: Path, onVideoCaptured: (CaptureResult<Path>) -> Unit)
    fun resumeRecording()
    fun pauseRecording()
    fun stopRecording()

    fun muteRecording(isMuted: Boolean)
}

internal expect class DefaultRecordController : RecordController {
    override var isRecording: Boolean
        internal set

    override fun startRecording(
        path: Path,
        onVideoCaptured: (CaptureResult<Path>) -> Unit
    )

    override fun resumeRecording()
    override fun pauseRecording()
    override fun stopRecording()
    override fun muteRecording(isMuted: Boolean)
}
