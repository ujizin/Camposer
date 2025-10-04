package com.ujizin.camposer.controller.record

import com.ujizin.camposer.result.CaptureResult
import kotlinx.io.files.Path

public interface RecordController {

    public val isRecording: Boolean

    public fun startRecording(path: Path, onVideoCaptured: (CaptureResult<Path>) -> Unit)
    public fun resumeRecording()
    public fun pauseRecording()
    public fun stopRecording()

    public fun muteRecording(isMuted: Boolean)
}

internal expect class DefaultRecordController : RecordController {
    override var isRecording: Boolean
        internal set

    override fun startRecording(
        path: Path,
        onVideoCaptured: (CaptureResult<Path>) -> Unit,
    )

    override fun resumeRecording()
    override fun pauseRecording()
    override fun stopRecording()
    override fun muteRecording(isMuted: Boolean)
}
