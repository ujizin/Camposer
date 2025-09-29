package com.ujizin.camposer.controller

import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.result.CaptureResult
import kotlinx.io.files.Path

// TODO missing take picture command
public class CameraController : RecordController {

    private var recordController: RecordController? = null

    override val isRecording: Boolean
        get() = recordController?.isRecording ?: false

    override fun startRecording(
        path: Path,
        onVideoCaptured: (CaptureResult<Path>) -> Unit
    ): Unit = recordRun { startRecording(path, onVideoCaptured) }

    override fun resumeRecording(): Unit = recordRun { resumeRecording() }

    override fun pauseRecording(): Unit = recordRun { pauseRecording() }

    override fun stopRecording(): Unit = recordRun { stopRecording() }

    override fun muteRecording(isMuted: Boolean): Unit = recordRun { muteRecording(isMuted) }

    internal fun initialize(recordController: RecordController) {
        this.recordController = recordController
    }

    private fun <T> recordRun(block: RecordController.() -> T): T {
        val recordController = recordController
        require(recordController != null) {
            "CameraController must be bind to CameraState!"
        }

        return recordController.block()
    }
}
