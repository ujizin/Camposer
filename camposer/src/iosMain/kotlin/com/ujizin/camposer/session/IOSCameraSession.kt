package com.ujizin.camposer.session

import com.ujizin.camposer.OrientationManager
import com.ujizin.camposer.error.AudioInputNotFoundException
import com.ujizin.camposer.extensions.captureDevice
import com.ujizin.camposer.extensions.firstIsInstanceOrNull
import com.ujizin.camposer.extensions.isFlashModeSupported
import com.ujizin.camposer.extensions.toDeviceInput
import com.ujizin.camposer.extensions.toVideoOrientation
import com.ujizin.camposer.extensions.tryAddInput
import com.ujizin.camposer.extensions.tryAddOutput
import com.ujizin.camposer.extensions.withConfigurationLock
import kotlinx.cinterop.COpaquePointer
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
import platform.Foundation.NSKeyValueObservingOptionNew
import platform.Foundation.addObserver
import platform.Foundation.removeObserver
import platform.UIKit.UIDevice
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.foundation.NSKeyValueObservingProtocol

@OptIn(ExperimentalForeignApi::class)
public class IOSCameraSession internal constructor(
    internal val captureSession: AVCaptureSession,
) {

    public var previewLayer: AVCaptureVideoPreviewLayer? = null

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

    private val observer = object : NSObject(), NSKeyValueObservingProtocol {
        override fun observeValueForKeyPath(
            keyPath: String?,
            ofObject: Any?,
            change: Map<Any?, *>?,
            context: COpaquePointer?,
        ) {
            // TODO isStreaming
            println("changed valued: $change")
        }
    }

    public fun addOutput(output: AVCaptureOutput): Boolean = captureSession.tryAddOutput(output)

    public fun removeOutput(output: AVCaptureOutput): Unit = captureSession.removeOutput(output)

    internal fun start(
        captureOutput: AVCaptureOutput,
        position: AVCaptureDevicePosition,
        gravity: AVLayerVideoGravity,
        isMuted: Boolean,
        presets: List<AVCaptureSessionPreset>,
    ) {
        captureSession.beginConfiguration()

        setCameraPreset(presets)
        setCameraPosition(position)
        setAudioEnabled(!isMuted)
        captureSession.tryAddOutput(captureOutput)

        captureSession.commitConfiguration()

        previewLayer = AVCaptureVideoPreviewLayer(session = captureSession).apply {
            setPreviewGravity(gravity)
        }

        previewLayer?.addObserver(
            observer = observer,
            forKeyPath = PREVIEWING_KEY_PATH,
            options = NSKeyValueObservingOptionNew,
            context = null
        )

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
        previewLayer?.videoGravity = gravity
    }

    internal fun renderPreviewLayer(view: UIView) = previewLayer?.apply {
        view.layer.addSublayer(this)
        setFrame(view.bounds)
        connection?.videoOrientation = UIDevice.currentDevice.orientation.toVideoOrientation()
    }

    internal fun setCameraPosition(position: AVCaptureDevicePosition) {
        if (_captureDeviceInput?.device?.position == position) {
            return
        }

        previewLayer?.connection?.setEnabled(false)

        // Remove the previous one
        if (_captureDeviceInput != null) {
            captureSession.removeInput(captureDeviceInput)
        }

        _captureDeviceInput = position.captureDevice.toDeviceInput()
        captureSession.tryAddInput(captureDeviceInput)

        previewLayer?.connection?.setEnabled(true)
    }

    internal fun setAudioEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            captureSession.tryAddInput(audioInput)
        } else {
            captureSession.removeInput(audioInput)
        }
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
        device.removeObserver(observer, PREVIEWING_KEY_PATH)
        _captureDeviceInput = null
        captureSession.stopRunning()
        previewLayer?.removeFromSuperlayer()
        previewLayer = null
    }

    private companion object {
        private const val PREVIEWING_KEY_PATH = "previewing"
    }
}