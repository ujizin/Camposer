package com.ujizin.camposer.helper

import com.ujizin.camposer.extensions.captureDevice
import com.ujizin.camposer.utils.executeWithErrorHandling
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDevicePositionUnspecified
import platform.AVFoundation.AVCaptureExposureModeAutoExpose
import platform.AVFoundation.AVCaptureFlashMode
import platform.AVFoundation.AVCaptureFocusModeAutoFocus
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPreset1280x720
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeLeft
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoOrientationPortraitUpsideDown
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.exposureMode
import platform.AVFoundation.exposurePointOfInterest
import platform.AVFoundation.flashMode
import platform.AVFoundation.focusMode
import platform.AVFoundation.focusPointOfInterest
import platform.AVFoundation.hasTorch
import platform.AVFoundation.isExposurePointOfInterestSupported
import platform.AVFoundation.isFocusPointOfInterestSupported
import platform.AVFoundation.isTorchAvailable
import platform.AVFoundation.position
import platform.AVFoundation.torchMode
import platform.AVFoundation.videoZoomFactor
import platform.CoreGraphics.CGPoint
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation
import platform.UIKit.UIView
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalForeignApi::class, ExperimentalAtomicApi::class)
internal class IOSCameraController {

    private val captureSession = AVCaptureSession()

    internal var previewLayer: AVCaptureVideoPreviewLayer? = null

    private lateinit var captureDeviceInput: AVCaptureDeviceInput

    private var isConfigurationLocked = AtomicBoolean(false)

    private val _cameraPositionState = MutableStateFlow(AVCaptureDevicePositionUnspecified)
    internal val cameraPositionState = _cameraPositionState.asStateFlow()


    internal val device: AVCaptureDevice
        get() = captureDeviceInput.device

    internal val isRunning: Boolean
        get() = captureSession.isRunning()

    val isFocusOnTapSupported: Boolean
        get() = device.isFocusPointOfInterestSupported() || device.isExposurePointOfInterestSupported()

    fun start(view: UIView, position: AVCaptureDevicePosition) {
        captureSession.beginConfiguration()
        captureSession.sessionPreset = AVCaptureSessionPreset1280x720

        switchInputCamera(position)

        val output = AVCapturePhotoOutput()
        if (captureSession.canAddOutput(output)) {
            captureSession.addOutput(output)
            output.setHighResolutionCaptureEnabled(true)
        }

        captureSession.commitConfiguration()

        previewLayer = AVCaptureVideoPreviewLayer(session = captureSession).apply {
            videoGravity = AVLayerVideoGravityResizeAspectFill
            view.layer.addSublayer(this)
        }

        captureSession.startRunning()
    }

    fun render(view: UIView) {
        previewLayer?.apply {
            setFrame(view.bounds)
            connection?.videoOrientation = currentVideoOrientation()
        }
    }

    fun switchInputCamera(position: AVCaptureDevicePosition) {
        if (::captureDeviceInput.isInitialized) {
            if (captureDeviceInput.device.position == position) {
                return
            }
            captureSession.removeInput(captureDeviceInput)
        }

        executeWithErrorHandling { ptr ->
            captureDeviceInput = AVCaptureDeviceInput.deviceInputWithDevice(
                position.captureDevice,
                ptr
            )!!
            if (captureSession.canAddInput(captureDeviceInput)) {
                captureSession.addInput(captureDeviceInput)
            }
        }

        _cameraPositionState.update {position }
    }

    fun setFocusPoint(focusPoint: CValue<CGPoint>) = withConfigurationLock {
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

    fun setTorchEnabled(isEnabled: Boolean) {
        if (!device.hasTorch || !device.isTorchAvailable()) return
        withConfigurationLock {
            torchMode = if (isEnabled) AVCaptureTorchModeOn else AVCaptureTorchModeOff
        }
    }

    fun setZoom(zoomRatio: Float) = withConfigurationLock {
        videoZoomFactor = zoomRatio.toDouble()
    }

    fun setFlashMode(mode: AVCaptureFlashMode) = withConfigurationLock {
        flashMode = mode
    }

    private fun withConfigurationLock(block: AVCaptureDevice.() -> Unit) = when {
        isConfigurationLocked.compareAndExchange(
            expectedValue = true,
            newValue = true
        ) -> device.block()

        else -> executeWithErrorHandling { nsErrorPtr ->
            try {
                device.lockForConfiguration(nsErrorPtr)
                device.block()
            } finally {
                device.unlockForConfiguration()
            }
        }
    }

    private fun currentVideoOrientation(): AVCaptureVideoOrientation {
        val orientation = UIDevice.currentDevice.orientation
        return when (orientation) {
            UIDeviceOrientation.UIDeviceOrientationPortrait -> AVCaptureVideoOrientationPortrait
            UIDeviceOrientation.UIDeviceOrientationPortraitUpsideDown -> AVCaptureVideoOrientationPortraitUpsideDown
            UIDeviceOrientation.UIDeviceOrientationLandscapeLeft -> AVCaptureVideoOrientationLandscapeRight
            UIDeviceOrientation.UIDeviceOrientationLandscapeRight -> AVCaptureVideoOrientationLandscapeLeft
            else -> AVCaptureVideoOrientationPortrait
        }
    }

    fun dispose() {
        captureSession.stopRunning()
        previewLayer = null
    }
}