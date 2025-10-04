package com.ujizin.camposer.controller.camera

import com.ujizin.camposer.command.TakePictureCommand
import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.result.CaptureResult
import kotlinx.io.files.Path

public expect class CameraController : RecordController, TakePictureCommand {

    public constructor()

    override val isRecording: Boolean
    override fun startRecording(
        path: Path,
        onVideoCaptured: (CaptureResult<Path>) -> Unit,
    )

    override fun resumeRecording()
    override fun pauseRecording()
    override fun stopRecording()
    override fun muteRecording(isMuted: Boolean)
    override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit)
    override fun takePicture(
        path: Path,
        onImageCaptured: (CaptureResult<Path>) -> Unit,
    )
}
