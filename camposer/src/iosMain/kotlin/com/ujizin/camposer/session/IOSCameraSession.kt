package com.ujizin.camposer.session

import com.ujizin.camposer.OrientationManager
import com.ujizin.camposer.error.AudioInputNotFoundException
import com.ujizin.camposer.extensions.captureDevice
import com.ujizin.camposer.extensions.firstIsInstanceOrNull
import com.ujizin.camposer.extensions.isFlashModeSupported
import com.ujizin.camposer.extensions.toDeviceInput
import com.ujizin.camposer.extensions.tryAddInput
import com.ujizin.camposer.extensions.tryAddOutput
import com.ujizin.camposer.extensions.withConfigurationLock
import com.ujizin.camposer.manager.PreviewManager
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDevice.Companion.defaultDeviceWithMediaType
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePosition
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
import platform.UIKit.UIView
@OptIn(ExperimentalForeignApi::class)
public class IOSCameraSession internal constructor(
    internal val captureSession: AVCaptureSession,
    public val previewManager: PreviewManager,
) {

    private var _captureDeviceInput: AVCaptureDeviceInput? = null

    public val captureDeviceInput: AVCaptureDeviceInput
        get() = _captureDeviceInput!!
    public val device: AVCaptureDevice
        get() = captureDeviceInput.device

    public val isRunning: Boolean
        get() = captureSession.isRunning()

    internal val orientationListener: OrientationManager = OrientationManager()

    private val audioInput = defaultDeviceWithMediaType(AVMediaTypeAudio)?.toDeviceInput()
        ?: throw AudioInputNotFoundException()

    public fun addOutput(output: AVCaptureOutput): Boolean = captureSession.tryAddOutput(output)

    public fun removeOutput(output: AVCaptureOutput): Unit = captureSession.removeOutput(output)

    internal fun start(
        captureOutput: AVCaptureOutput,
        position: AVCaptureDevicePosition,
        isMuted: Boolean,
        presets: List<AVCaptureSessionPreset>,
    ) {
        captureSession.beginConfiguration()

        setCameraPreset(presets)
        setCameraPosition(position)
        setAudioEnabled(!isMuted)
        captureSession.tryAddOutput(captureOutput)
        captureSession.commitConfiguration()

        previewManager.start(captureSession)

        orientationListener.start()
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

    internal fun setPreviewGravity(gravity: AVLayerVideoGravity) {
        previewManager.setGravity(gravity)
    }

    internal fun renderPreviewLayer(view: UIView) {
        previewManager.attachView(view)
    }

    internal fun setCameraPosition(position: AVCaptureDevicePosition) {
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

    internal fun setAudioEnabled(isEnabled: Boolean) = when {
        isEnabled -> captureSession.tryAddInput(audioInput)
        else -> captureSession.removeInput(audioInput)
    }

    internal fun setFocusPoint(focusPoint: CValue<CGPoint>) = device.withConfigurationLock {
        if (isFocusPointOfInterestSupported()) {
            focusPointOfInterest = focusPoint
            focusMode = AVCaptureFocusModeAutoFocus
        }

        if (isExposurePointOfInterestSupported()) {
            exposurePointOfInterest = focusPoint
            exposureMode = AVCaptureExposureModeAutoExpose
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

    public fun release() {
        orientationListener.stop()
        _captureDeviceInput = null
        captureSession.stopRunning()
        previewManager.detachView()
    }
}
