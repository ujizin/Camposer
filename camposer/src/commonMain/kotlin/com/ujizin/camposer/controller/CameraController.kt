package com.ujizin.camposer.controller

import com.ujizin.camposer.command.TakePictureCommand
import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.result.CaptureResult
import kotlinx.io.files.Path

public class CameraController : RecordController, TakePictureCommand {

    private var recordController: RecordController? = null
    private var takePictureCommand: TakePictureCommand? = null

    override val isRecording: Boolean
        get() = recordController?.isRecording ?: false

    override fun startRecording(
        path: Path,
        onVideoCaptured: (CaptureResult<Path>) -> Unit
    ): Unit = bindRun { startRecording(path, onVideoCaptured) }

    override fun resumeRecording(): Unit = recordController.bindRun { resumeRecording() }

    override fun pauseRecording(): Unit = recordController.bindRun { pauseRecording() }

    override fun stopRecording(): Unit = recordController.bindRun { stopRecording() }

    override fun muteRecording(isMuted: Boolean): Unit = recordController.bindRun {
        muteRecording(isMuted)
    }

    override fun takePicture(
        onImageCaptured: (CaptureResult<ByteArray>) -> Unit
    ): Unit = takePictureCommand.bindRun { takePicture(onImageCaptured) }

    override fun takePicture(
        path: Path,
        onImageCaptured: (CaptureResult<Path>) -> Unit
    ): Unit = takePictureCommand.bindRun { takePicture(path, onImageCaptured) }

    internal fun initialize(
        recordController: RecordController,
        takePictureCommand: TakePictureCommand,
    ) {
        this.recordController = recordController
        this.takePictureCommand = takePictureCommand
    }

    private fun <T, R> T?.bindRun(block: T.() -> R): R {
        require(this != null) {
            "CameraController must be bind to CameraState first to be used!"
        }

        return block()
    }
}
