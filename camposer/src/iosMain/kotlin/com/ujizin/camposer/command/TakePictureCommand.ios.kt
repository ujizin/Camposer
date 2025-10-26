package com.ujizin.camposer.command

import com.ujizin.camposer.extensions.toCaptureResult
import com.ujizin.camposer.result.CaptureResult
import com.ujizin.camposer.session.IOSCameraSession
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.flashMode
import platform.AVFoundation.position

internal actual class DefaultTakePictureCommand(
    private val iosCameraSession: IOSCameraSession,
    private val cameraState: CameraState,
    private val takePictureCommand: IOSTakePictureCommand = IOSTakePictureCommand(
        captureSession = iosCameraSession.captureSession,
    ),
) : TakePictureCommand {

    private val captureDeviceInput: AVCaptureDeviceInput
        get() = iosCameraSession.captureDeviceInput

    actual override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit) {
        require(cameraState.captureMode == CaptureMode.Image) {
            "Capture mode must be CaptureMode.Image"
        }

        takePictureCommand(
            isMirrorEnabled = captureDeviceInput.device.position == AVCaptureDevicePositionFront,
            flashMode = cameraState.flashMode.mode,
            videoOrientation = iosCameraSession.orientationListener.currentOrientation.toVideoOrientation(),
            onPictureCaptured = onPictureCaptured(onImageCaptured),
        )
    }

    actual override fun takePicture(filename: String, onImageCaptured: (CaptureResult<String>) -> Unit) {
        require(cameraState.captureMode == CaptureMode.Image) {
            "Capture mode must be CaptureMode.Image"
        }

        takePictureCommand(
            filename = filename,
            isMirrorEnabled = captureDeviceInput.device.position == AVCaptureDevicePositionFront,
            flashMode = captureDeviceInput.device.flashMode,
            videoOrientation = iosCameraSession.orientationListener.currentOrientation.toVideoOrientation(),
            onPictureCaptured = onPictureCaptured(onImageCaptured),
        )
    }
    private fun <T> onPictureCaptured(
        onImageCaptured: (CaptureResult<T>) -> Unit
    ): (Result<T>) -> Unit = { result ->
        onImageCaptured(result.toCaptureResult())

        // iOS disable torch when flash mode and torch is enabled altogether
        if (cameraState.isTorchEnabled && cameraState.flashMode == FlashMode.On) {
            iosCameraSession.setTorchEnabled(true)
        }
    }
}
