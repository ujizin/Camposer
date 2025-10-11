package com.ujizin.camposer.session

import com.ujizin.camposer.controller.command.IOSTakePictureCommand
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
import kotlinx.io.files.Path
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDevice.Companion.defaultDeviceWithMediaType
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDevicePositionFront
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
public class IOSCameraSession internal constructor(
    internal val captureSession: AVCaptureSession = AVCaptureSession(),
    private val takePictureCommand: IOSTakePictureCommand = IOSTakePictureCommand(captureSession),
) {

    private val audioInput = defaultDeviceWithMediaType(AVMediaTypeAudio)?.toDeviceInput()
        ?: throw AudioInputNotFoundException()

    public var previewLayer: AVCaptureVideoPreviewLayer? = null

    private var _captureDeviceInput: AVCaptureDeviceInput? = null
    public val captureDeviceInput: AVCaptureDeviceInput
        get() = _captureDeviceInput!!

    public val device: AVCaptureDevice
        get() = captureDeviceInput.device

    public val isRunning: Boolean
        get() = captureSession.isRunning()

    internal fun start(
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

    internal fun setCameraPreset(presets: List<AVCaptureSessionPreset>) {
        for (preset in presets) {
            if (captureSession.canSetSessionPreset(preset)) {
                captureSession.sessionPreset = preset
                break
            }
        }
    }

    internal fun renderPreviewLayer(view: UIView) = previewLayer?.apply {
        setFrame(view.bounds)
        connection?.videoOrientation = UIDevice.currentDevice.orientation.toVideoOrientation()
    }

    internal fun takePicture(onPictureCaptured: (Result<ByteArray>) -> Unit): Unit =
        takePictureCommand(
            isMirrorEnabled = captureDeviceInput.device.position == AVCaptureDevicePositionFront,
            flashMode = captureDeviceInput.device.flashMode,
            onPictureCaptured = onPictureCaptured,
        )

    internal fun takePicture(
        path: Path,
        onPictureCaptured: (Result<Path>) -> Unit,
    ): Unit = takePictureCommand(
        path = path,
        isMirrorEnabled = captureDeviceInput.device.position == AVCaptureDevicePositionFront,
        flashMode = captureDeviceInput.device.flashMode,
        onPictureCaptured = onPictureCaptured,
    )

    internal fun setCameraPosition(
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
    }

    internal fun setAudioEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            captureSession.tryAddInput(audioInput)
        } else {
            captureSession.removeInput(audioInput)
        }
    }

    internal fun setFocusPoint(focusPoint: CValue<CGPoint>) = device.withConfigurationLock {
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

    internal fun setTorchEnabled(isEnabled: Boolean) {
        if (!device.hasTorch || !device.isTorchAvailable()) return
        device.withConfigurationLock {
            torchMode = if (isEnabled) AVCaptureTorchModeOn else AVCaptureTorchModeOff
        }
    }

    internal fun setFlashMode(mode: AVCaptureFlashMode) {
        val isFlashModeAvailable = device.hasFlash && device.isFlashAvailable()
        if (!isFlashModeAvailable || !captureSession.isFlashModeSupported(mode)) {
            return
        }

        device.withConfigurationLock { flashMode = mode }
    }

    internal fun setCameraOutputQuality(
        quality: AVCapturePhotoQualityPrioritization,
        highResolutionEnabled: Boolean,
    ) {
        val output = captureSession.outputs.firstIsInstanceOrNull<AVCapturePhotoOutput>()
        output?.setMaxPhotoQualityPrioritization(quality)
        output?.setHighResolutionCaptureEnabled(highResolutionEnabled)
    }

    public fun addOutput(output: AVCaptureOutput): Boolean = captureSession.tryAddOutput(output)

    public fun removeOutput(output: AVCaptureOutput): Unit = captureSession.removeOutput(output)

    public fun release() {
        _captureDeviceInput = null
        captureSession.stopRunning()
        previewLayer?.removeFromSuperlayer()
        previewLayer = null
    }
}