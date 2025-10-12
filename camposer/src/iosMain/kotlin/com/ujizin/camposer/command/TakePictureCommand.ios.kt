package com.ujizin.camposer.command

import com.ujizin.camposer.config.CameraConfig
import com.ujizin.camposer.config.properties.CaptureMode
import com.ujizin.camposer.extensions.toCaptureResult
import com.ujizin.camposer.result.CaptureResult
import com.ujizin.camposer.session.IOSCameraSession
import kotlinx.io.files.Path
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.flashMode
import platform.AVFoundation.position

internal actual class DefaultTakePictureCommand(
    private val iosCameraSession: IOSCameraSession,
    private val cameraConfig: CameraConfig,
    private val takePictureCommand: IOSTakePictureCommand = IOSTakePictureCommand(
        captureSession = iosCameraSession.captureSession,
    ),
) : TakePictureCommand {

    private val captureDeviceInput: AVCaptureDeviceInput
        get() = iosCameraSession.captureDeviceInput

    actual override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit) {
        require(cameraConfig.captureMode == CaptureMode.Image) {
            "Capture mode must be CaptureMode.Image"
        }

        takePictureCommand(
            isMirrorEnabled = captureDeviceInput.device.position == AVCaptureDevicePositionFront,
            flashMode = captureDeviceInput.device.flashMode,
            videoOrientation = iosCameraSession.orientationListener.currentOrientation.toVideoOrientation(),
            onPictureCaptured = { result -> onImageCaptured(result.toCaptureResult()) },
        )
    }

    actual override fun takePicture(path: Path, onImageCaptured: (CaptureResult<Path>) -> Unit) {
        require(cameraConfig.captureMode == CaptureMode.Image) {
            "Capture mode must be CaptureMode.Image"
        }

        takePictureCommand(
            path = path,
            isMirrorEnabled = captureDeviceInput.device.position == AVCaptureDevicePositionFront,
            flashMode = captureDeviceInput.device.flashMode,
            videoOrientation = iosCameraSession.orientationListener.currentOrientation.toVideoOrientation(),
            onPictureCaptured = { result -> onImageCaptured(result.toCaptureResult()) },
        )
    }
}