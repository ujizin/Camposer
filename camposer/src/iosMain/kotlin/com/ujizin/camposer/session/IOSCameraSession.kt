package com.ujizin.camposer.session

import com.ujizin.camposer.internal.OrientationManager
import com.ujizin.camposer.internal.error.AudioInputNotFoundException
import com.ujizin.camposer.internal.extensions.firstIsInstanceOrNull
import com.ujizin.camposer.internal.extensions.isFlashModeSupported
import com.ujizin.camposer.internal.extensions.toDeviceInput
import com.ujizin.camposer.internal.extensions.tryAddInput
import com.ujizin.camposer.internal.extensions.tryAddOutput
import com.ujizin.camposer.internal.extensions.withConfigurationLock
import com.ujizin.camposer.internal.utils.DispatchQueue.cameraQueue
import com.ujizin.camposer.manager.PreviewManager
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDevice.Companion.defaultDeviceWithMediaType
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDeviceSubjectAreaDidChangeNotification
import platform.AVFoundation.AVCaptureExposureModeAutoExpose
import platform.AVFoundation.AVCaptureExposureModeContinuousAutoExposure
import platform.AVFoundation.AVCaptureFlashMode
import platform.AVFoundation.AVCaptureFocusModeAutoFocus
import platform.AVFoundation.AVCaptureFocusModeContinuousAutoFocus
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoQualityPrioritization
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVCaptureVideoStabilizationMode
import platform.AVFoundation.AVFrameRateRange
import platform.AVFoundation.AVLayerVideoGravity
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
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
import platform.AVFoundation.setSubjectAreaChangeMonitoringEnabled
import platform.AVFoundation.torchMode
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGPointMake
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async

@OptIn(ExperimentalForeignApi::class)
public class IOSCameraSession internal constructor(
    internal val captureSession: AVCaptureSession,
    internal val previewManager: PreviewManager,
) {

    private var _captureDeviceInput: AVCaptureDeviceInput? = null

    public val captureDeviceInput: AVCaptureDeviceInput
        get() = _captureDeviceInput!!

    public val device: AVCaptureDevice
        get() = captureDeviceInput.device

    public val isRunning: Boolean
        get() = captureSession.isRunning()

    internal val orientationListener: OrientationManager = OrientationManager()

    internal val isFocusSupported: Boolean
        get() = device.isFocusPointOfInterestSupported() || device.isExposurePointOfInterestSupported()

    private val audioInput = defaultDeviceWithMediaType(AVMediaTypeAudio)?.toDeviceInput()
        ?: throw AudioInputNotFoundException()

    internal val frameRateRanges: List<AVFrameRateRange>
        get() = device.activeFormat.videoSupportedFrameRateRanges
            .filterIsInstance<AVFrameRateRange>()

    internal val minFrameRate: Int
        get() = frameRateRanges.minOf { it.minFrameRate }.toInt()

    internal val maxFrameRate: Int
        get() = frameRateRanges.maxOf { it.maxFrameRate }.toInt()

    public val previewLayer: AVCaptureVideoPreviewLayer = previewManager.videoPreviewLayer

    private val completeFocusObserver = object : NSObject() {
        @OptIn(BetaInteropApi::class)
        @ObjCAction
        private fun onFocusCompleted(notification: NSNotification?) = onFocusCompleted()
    }

    public fun addOutput(output: AVCaptureOutput): Boolean = captureSession.tryAddOutput(output)

    public fun removeOutput(output: AVCaptureOutput): Unit = captureSession.removeOutput(output)

    internal fun start(
        captureOutput: AVCaptureOutput,
        device: AVCaptureDevice,
        isMuted: Boolean,
    ) = dispatch_async(cameraQueue) {
        captureSession.beginConfiguration()

        setCaptureDevice(device)
        setAudioEnabled(!isMuted)
        captureSession.tryAddOutput(captureOutput)
        captureSession.commitConfiguration()

        previewManager.start(captureSession)

        orientationListener.start()
        captureSession.startRunning()
    }

    internal fun setFrameRate(frameRate: Int) {
        val captureDevice = device
        if (frameRate !in minFrameRate..maxFrameRate) {
            return
        }

        val minFps = CMTimeMake(1, frameRate)
        val maxFps = CMTimeMake(1, frameRate)

        captureDevice.withConfigurationLock {
            captureDevice.activeVideoMinFrameDuration = minFps
            captureDevice.activeVideoMaxFrameDuration = maxFps
        }
    }

    internal fun isVideoStabilizationSupported(mode: AVCaptureVideoStabilizationMode): Boolean {
        return device.activeFormat.isVideoStabilizationModeSupported(mode)
    }

    internal fun setVideoStabilization(mode: AVCaptureVideoStabilizationMode) {
        val output = captureSession.outputs.firstIsInstanceOrNull<AVCaptureMovieFileOutput>()
        val videoConnection = output?.connectionWithMediaType(AVMediaTypeVideo)

        if (isVideoStabilizationSupported(mode)) {
            return
        }

        device.withConfigurationLock {
            videoConnection?.preferredVideoStabilizationMode = mode
        }
    }

    internal fun setDeviceFormat(format: AVCaptureDeviceFormat) {
        if (!device.formats().contains(format)) {
            return
        }

        device.withConfigurationLock { device.setActiveFormat(format) }
    }

    internal fun setPreviewGravity(gravity: AVLayerVideoGravity) {
        previewManager.setGravity(gravity)
    }

    internal fun renderPreviewLayer(view: UIView) {
        previewManager.attachView(view)
    }

    internal fun setCaptureDevice(device: AVCaptureDevice) {
        if (_captureDeviceInput?.device == device) {
            return
        }

        // Remove the previous one
        if (_captureDeviceInput != null) {
            captureSession.removeInput(captureDeviceInput)
        }

        _captureDeviceInput = device.toDeviceInput()
        captureSession.tryAddInput(captureDeviceInput)
    }

    internal fun setAudioEnabled(isEnabled: Boolean) {
        when {
            isEnabled -> captureSession.tryAddInput(audioInput)
            else -> captureSession.removeInput(audioInput)
        }
    }

    internal fun setFocusPoint(focusPoint: CValue<CGPoint>) = device.withConfigurationLock {
        if (isExposurePointOfInterestSupported()) {
            exposurePointOfInterest = focusPoint
            exposureMode = AVCaptureExposureModeAutoExpose
        }

        if (isFocusPointOfInterestSupported()) {
            focusPointOfInterest = focusPoint
            focusMode = AVCaptureFocusModeAutoFocus
        }

        val notificationCenter = NSNotificationCenter.defaultCenter
        notificationCenter.removeObserver(
            observer = completeFocusObserver,
            name = AVCaptureDeviceSubjectAreaDidChangeNotification,
            `object` = null,
        )

        device.setSubjectAreaChangeMonitoringEnabled(true)

        notificationCenter.addObserver(
            observer = completeFocusObserver,
            selector = NSSelectorFromString("${::onFocusCompleted.name}:"),
            name = AVCaptureDeviceSubjectAreaDidChangeNotification,
            `object` = null,
        )
    }

    @OptIn(BetaInteropApi::class)
    @ObjCAction
    private fun onFocusCompleted(): Unit = device.withConfigurationLock {
        val centerFocusPoint = CGPointMake(0.5, 0.5)
        if (isFocusPointOfInterestSupported()) {
            focusPointOfInterest = centerFocusPoint
            focusMode = AVCaptureFocusModeContinuousAutoFocus
        }

        if (isExposurePointOfInterestSupported()) {
            exposurePointOfInterest = centerFocusPoint
            exposureMode = AVCaptureExposureModeContinuousAutoExposure
        }

        device.setSubjectAreaChangeMonitoringEnabled(false)

        NSNotificationCenter.defaultCenter.removeObserver(
            observer = completeFocusObserver,
            name = AVCaptureDeviceSubjectAreaDidChangeNotification,
            `object` = null,
        )
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
