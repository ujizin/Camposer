package com.ujizin.camposer.controller.camera

import CameraControllerContract
import com.ujizin.camposer.command.TakePictureCommand
import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.result.CaptureResult
import kotlinx.io.files.Path

public abstract class CommonCameraController<RC : RecordController, TPC : TakePictureCommand> :
    CameraControllerContract {
    protected var recordController: RC? = null
    protected var takePictureCommand: TPC? = null

    override val isMuted: Boolean
        get() = recordController?.isMuted ?: false

    override val isRecording: Boolean
        get() = recordController?.isRecording ?: false

    override fun startRecording(
        path: Path,
        onVideoCaptured: (CaptureResult<Path>) -> Unit,
    ): Unit = bindRun { startRecording(path, onVideoCaptured) }

    override fun resumeRecording(): Unit = recordController.bindRun { resumeRecording() }

    override fun pauseRecording(): Unit = recordController.bindRun { pauseRecording() }

    override fun stopRecording(): Unit = recordController.bindRun { stopRecording() }

    override fun muteRecording(isMuted: Boolean): Unit = recordController.bindRun {
        muteRecording(isMuted)
    }

    override fun takePicture(
        onImageCaptured: (CaptureResult<ByteArray>) -> Unit,
    ): Unit = takePictureCommand.bindRun { takePicture(onImageCaptured) }

    override fun takePicture(
        path: Path,
        onImageCaptured: (CaptureResult<Path>) -> Unit,
    ): Unit = takePictureCommand.bindRun { takePicture(path, onImageCaptured) }

    internal fun initialize(
        recordController: RC,
        takePictureCommand: TPC,
    ) {
        this.recordController = recordController
        this.takePictureCommand = takePictureCommand
    }

    protected fun <T, R> T?.bindRun(block: T.() -> R): R {
        require(this != null) {
            "CameraController must be bind to CameraState first to be used!"
        }

        return block()
    }
}