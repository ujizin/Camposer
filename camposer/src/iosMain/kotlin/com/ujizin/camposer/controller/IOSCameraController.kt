package com.ujizin.camposer.controller

import com.ujizin.camposer.controller.command.RecordVideoController
import com.ujizin.camposer.controller.command.TakePictureCommand
import com.ujizin.camposer.error.AudioInputNotFoundException
import com.ujizin.camposer.extensions.captureDevice
import com.ujizin.camposer.extensions.firstIsInstanceOrNull
import com.ujizin.camposer.extensions.isFlashModeSupported
import com.ujizin.camposer.extensions.toDeviceInput
import com.ujizin.camposer.extensions.toVideoOrientation
import com.ujizin.camposer.extensions.tryAddInput
import com.ujizin.camposer.extensions.tryAddOutput
import com.ujizin.camposer.extensions.withConfigurationLock
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.io.files.Path
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDevice.Companion.defaultDeviceWithMediaType
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDevicePositionUnspecified
import platform.AVFoundation.AVCaptureExposureModeAutoExpose
import platform.AVFoundation.AVCaptureFlashMode
import platform.AVFoundation.AVCaptureFocusModeAutoFocus
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoQualityPrioritization
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPreset
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravity
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.exposureMode
import platform.AVFoundation.exposurePointOfInterest
import platform.AVFoundation.flashMode
import platform.AVFoundation.focusMode
import platform.AVFoundation.focusPointOfInterest
import platform.AVFoundation.hasFlash
import platform.AVFoundation.hasTorch
import platform.AVFoundation.isExposurePointOfInterestSupported
import platform.AVFoundation.isFlashAvailable
import platform.AVFoundation.isFocusPointOfInterestSupported
import platform.AVFoundation.isTorchAvailable
import platform.AVFoundation.position
import platform.AVFoundation.torchMode
import platform.CoreGraphics.CGPoint
import platform.UIKit.UIDevice
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
internal class IOSCameraController(
    private val captureSession: AVCaptureSession = AVCaptureSession(),
    private val takePictureCommand: TakePictureCommand = TakePictureCommand(captureSession),
    private val recordVideoController: RecordVideoController = RecordVideoController(captureSession),
) {

    internal var previewLayer: AVCaptureVideoPreviewLayer? = null

    private var _captureDeviceInput: AVCaptureDeviceInput? = null
    private val captureDeviceInput: AVCaptureDeviceInput
        get() = _captureDeviceInput!!

    internal val device: AVCaptureDevice
        get() = captureDeviceInput.device

    private val _cameraPositionState = MutableStateFlow(AVCaptureDevicePositionUnspecified)
    val cameraPositionState = _cameraPositionState.asStateFlow()

    val isRunning: Boolean
        get() = captureSession.isRunning()

    val isFocusOnTapSupported: Boolean
        get() = device.isFocusPointOfInterestSupported() || device.isExposurePointOfInterestSupported()

    fun start(
        view: UIView,
        output: AVCaptureOutput,
        position: AVCaptureDevicePosition,
        gravity: AVLayerVideoGravity,
        isMuted: Boolean,
        presets: List<AVCaptureSessionPreset>,
    ) {
        captureSession.beginConfiguration()

        setCameraPreset(presets)
        setCameraPosition(position)
        setAudioEnabled(!isMuted)
        captureSession.tryAddOutput(output)

        captureSession.commitConfiguration()

        previewLayer = AVCaptureVideoPreviewLayer(session = captureSession).apply {
            videoGravity = gravity
            view.layer.addSublayer(this)
        }

        captureSession.startRunning()
    }

    fun setCameraPreset(presets: List<AVCaptureSessionPreset>) {
        for (preset in presets) {
            if (captureSession.canSetSessionPreset(preset)) {
                captureSession.sessionPreset = preset
                break
            }
        }
    }

    fun renderPreviewLayer(view: UIView) = previewLayer?.apply {
        setFrame(view.bounds)
        connection?.videoOrientation = UIDevice.currentDevice.orientation.toVideoOrientation()
    }

    fun startRecording(
        path: Path,
        onVideoCapture: (Result<Path>) -> Unit
    ) = recordVideoController.start(
        isMirrorEnabled = captureDeviceInput.device.position == AVCaptureDevicePositionFront,
        path = path,
        onVideoCapture = onVideoCapture,
    )

    fun stopRecording() = recordVideoController.stop()

    fun pauseRecording() = recordVideoController.pause()

    fun resumeRecording() = recordVideoController.resume()

    fun takePicture(onPictureCaptured: (Result<ByteArray>) -> Unit) = takePictureCommand(
        isMirrorEnabled = captureDeviceInput.device.position == AVCaptureDevicePositionFront,
        flashMode = captureDeviceInput.device.flashMode,
        onPictureCaptured = onPictureCaptured,
    )

    fun takePicture(path: Path, onPictureCaptured: (Result<Path>) -> Unit) = takePictureCommand(
        path = path,
        isMirrorEnabled = captureDeviceInput.device.position == AVCaptureDevicePositionFront,
        flashMode = captureDeviceInput.device.flashMode,
        onPictureCaptured = onPictureCaptured,
    )

    fun setCameraPosition(
        position: AVCaptureDevicePosition,
    ) {
        if (_captureDeviceInput?.device?.position == position) {
            return
        }

        // Remove the previous one
        if (_captureDeviceInput != null) {
            captureSession.removeInput(captureDeviceInput)
        }

        _captureDeviceInput = position.captureDevice.toDeviceInput()
        captureSession.tryAddInput(captureDeviceInput)

        _cameraPositionState.update { position }
    }

    fun setAudioEnabled(isEnabled: Boolean) {
        val audioInput = defaultDeviceWithMediaType(AVMediaTypeAudio)?.toDeviceInput()
            ?: throw AudioInputNotFoundException()

        if (isEnabled) {
            captureSession.tryAddInput(audioInput)
        } else {
            captureSession.removeInput(audioInput)
        }
    }

    fun setFocusPoint(focusPoint: CValue<CGPoint>) = device.withConfigurationLock {
        when {
            isFocusPointOfInterestSupported() -> {
                focusPointOfInterest = focusPoint
                focusMode = AVCaptureFocusModeAutoFocus
            }

            isExposurePointOfInterestSupported() -> {
                exposurePointOfInterest = focusPoint
                exposureMode = AVCaptureExposureModeAutoExpose
            }
        }
    }

    fun switchCameraOutput(old: AVCaptureOutput, new: AVCaptureOutput) {
        captureSession.removeOutput(old)
        captureSession.tryAddOutput(new)
    }

    fun setTorchEnabled(isEnabled: Boolean) {
        if (!device.hasTorch || !device.isTorchAvailable()) return
        device.withConfigurationLock {
            torchMode = if (isEnabled) AVCaptureTorchModeOn else AVCaptureTorchModeOff
        }
    }

    fun setFlashMode(mode: AVCaptureFlashMode) {
        val isFlashModeAvailable = device.hasFlash && device.isFlashAvailable()
        if (!isFlashModeAvailable || !captureSession.isFlashModeSupported(mode)) {
            return
        }

        device.withConfigurationLock { flashMode = mode }
    }

    fun setCameraOutputQuality(
        quality: AVCapturePhotoQualityPrioritization,
        highResolutionEnabled: Boolean,
    ) {
        val output = captureSession.outputs.firstIsInstanceOrNull<AVCapturePhotoOutput>()
        output?.setMaxPhotoQualityPrioritization(quality)
        output?.setHighResolutionCaptureEnabled(highResolutionEnabled)
    }

    fun release() {
        _captureDeviceInput = null
        captureSession.stopRunning()
        previewLayer?.removeFromSuperlayer()
        previewLayer = null
    }
}
