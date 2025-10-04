package com.ujizin.camposer.command

import com.ujizin.camposer.controller.record.RecordCaptureModeProvider
import com.ujizin.camposer.extensions.toCaptureResult
import com.ujizin.camposer.result.CaptureResult
import com.ujizin.camposer.session.IOSCameraSession
import com.ujizin.camposer.state.CaptureMode
import kotlinx.io.files.Path

internal actual class DefaultTakePictureCommand(
    private val cameraManager: IOSCameraSession,
    private val captureModeProvider: RecordCaptureModeProvider,
) : TakePictureCommand {

    actual override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit) {
        require(captureModeProvider.get() == CaptureMode.Image) {
            "Capture mode must be CaptureMode.Image"
        }
        cameraManager.takePicture { result -> onImageCaptured(result.toCaptureResult()) }
    }

    actual override fun takePicture(path: Path, onImageCaptured: (CaptureResult<Path>) -> Unit) {
        require(captureModeProvider.get() == CaptureMode.Image) {
            "Capture mode must be CaptureMode.Image"
        }
        cameraManager.takePicture(path) { result -> onImageCaptured(result.toCaptureResult()) }
    }
}