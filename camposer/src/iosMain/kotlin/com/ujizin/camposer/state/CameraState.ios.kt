package com.ujizin.camposer.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.extensions.captureDevice
import com.ujizin.camposer.extensions.withConfigurationLock
import com.ujizin.camposer.mapper.toAVCaptureDevicePosition
import com.ujizin.camposer.utils.executeWithErrorHandling
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureExposureModeAutoExpose
import platform.AVFoundation.AVCaptureFocusModeAutoFocus
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPreset1280x720
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.exposureMode
import platform.AVFoundation.exposurePointOfInterest
import platform.AVFoundation.exposureTargetOffset
import platform.AVFoundation.flashMode
import platform.AVFoundation.focusMode
import platform.AVFoundation.focusPointOfInterest
import platform.AVFoundation.hasTorch
import platform.AVFoundation.isExposurePointOfInterestSupported
import platform.AVFoundation.isFlashAvailable
import platform.AVFoundation.isFocusPointOfInterestSupported
import platform.AVFoundation.isTorchAvailable
import platform.AVFoundation.maxExposureTargetBias
import platform.AVFoundation.minExposureTargetBias
import platform.AVFoundation.position
import platform.AVFoundation.torchMode
import platform.AVFoundation.videoZoomFactor
import platform.CoreGraphics.CGPoint
import platform.UIKit.UIView
import kotlin.math.roundToInt

@OptIn(ExperimentalForeignApi::class)
public actual class CameraState {

    private val captureSession = AVCaptureSession()

    internal var previewLayer: AVCaptureVideoPreviewLayer? = null

    private lateinit var captureDeviceInput: AVCaptureDeviceInput

    private val captureDevice: AVCaptureDevice
        get() = captureDeviceInput.device

    internal actual var camSelector: CamSelector = CamSelector.Back
        set(value) {
            if (value == field) return
            field = value
            value.setupCameraInput()
        }

    internal actual var captureMode: CaptureMode
        get() = CaptureMode.Image
        set(value) {}

    internal actual var imageCaptureMode: ImageCaptureMode = ImageCaptureMode.MinLatency

    internal actual var imageCaptureTargetSize: ImageTargetSize?
        get() = ImageTargetSize()
        set(value) {}

    internal actual var flashMode: FlashMode = FlashMode.Off
        set(value) {
            if (field == value || !captureDevice.isFlashAvailable()) return
            field = value
            captureDevice.withConfigurationLock { flashMode = value.mode }
        }

    internal actual var scaleType: ScaleType = ScaleType.FillCenter

    internal actual var implementationMode: ImplementationMode = ImplementationMode.Performance

    internal actual var isImageAnalysisEnabled: Boolean
        get() = true
        set(value) {}

    internal actual var isFocusOnTapEnabled: Boolean = true


    // When blocked, this disable automatically, let's try to make enable again when appeared again
    internal actual var enableTorch: Boolean = false
        set(value) {
            if (field == value) return
            field = value

            setTorchEnabled(value)
        }

    private fun setTorchEnabled(isEnabled: Boolean) {
        if (!captureDevice.hasTorch || !captureDevice.isTorchAvailable()) return
        captureDevice.withConfigurationLock {
            torchMode = if (isEnabled) AVCaptureTorchModeOn else AVCaptureTorchModeOff
        }
    }

    internal var zoomRatio: Float
        get() = captureDevice.videoZoomFactor.toFloat()
        set(value) {
            if (zoomRatio == value || value !in minZoom..maxZoom) return
            captureDevice.withConfigurationLock {
                captureDevice.videoZoomFactor = value.toDouble()
            }
        }

    private var exposureCompensation: Int
        get() = captureDevice.exposureTargetOffset.roundToInt()
        set(value) {

        }

    public actual val initialExposure: Int by lazy { exposureCompensation }

    public actual val isZoomSupported: Boolean = true

    public actual var maxZoom: Float = 1F
        get() = captureDevice.activeFormat.videoMaxZoomFactor.toFloat()
        private set

    public actual var minZoom: Float = 1F
        private set

    public actual var minExposure: Int = 0
        get() = captureDevice.minExposureTargetBias.roundToInt()
        private set

    public actual var maxExposure: Int = 0
        get() = captureDevice.maxExposureTargetBias.roundToInt()
        private set

    public actual val isExposureSupported: Boolean
        get() = true

    public actual var isStreaming: Boolean = false
        internal set

    public actual var isFocusOnTapSupported: Boolean = false
        get() = captureDevice.isFocusPointOfInterestSupported() || captureDevice.isExposurePointOfInterestSupported()
        private set

    public actual var isInitialized: Boolean = false
        get() = captureSession.isRunning()
        private set

    public actual var hasFlashUnit: Boolean by mutableStateOf(false)
        private set

    public actual var hasTorchAvailable: Boolean by mutableStateOf(false)
        private set

    public actual var isRecording: Boolean
        get() = false
        set(value) {}


    internal actual var videoQualitySelector: QualitySelector
        get() = QualitySelector()
        set(value) {}

    internal var isPinchToZoomEnabled: Boolean = true
        private set

    init {
        camSelector.setupCameraInput()
    }

    @OptIn(ExperimentalForeignApi::class)
    internal fun initCamera(view: UIView) {
        captureSession.beginConfiguration()
        captureSession.sessionPreset = AVCaptureSessionPreset1280x720

        camSelector.setupCameraInput()

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
    }

    private fun CamSelector.setupCameraInput() {
        val position = toAVCaptureDevicePosition()
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
        hasTorchAvailable = captureDevice.isTorchAvailable()
        hasFlashUnit = captureDevice.isFlashAvailable()
    }

    internal fun startCamera() {
        captureSession.startRunning()
    }

    internal fun setFocusPoint(focusPoint: CValue<CGPoint>) {
        captureDevice.withConfigurationLock {
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
    }

    /**
     * Update all values from camera state.
     * */
    internal fun update(
        camSelector: CamSelector,
        captureMode: CaptureMode,
        scaleType: ScaleType,
        imageCaptureTargetSize: ImageTargetSize?,
        isImageAnalysisEnabled: Boolean,
        imageAnalyzer: ImageAnalyzer?,
        implementationMode: ImplementationMode,
        isFocusOnTapEnabled: Boolean,
        flashMode: FlashMode,
        zoomRatio: Float,
        imageCaptureMode: ImageCaptureMode,
        enableTorch: Boolean,
        videoQualitySelector: QualitySelector,
        exposureCompensation: Int,
        isPinchToZoomEnabled: Boolean,
    ) {
        this.camSelector = camSelector
        this.captureMode = captureMode
        this.scaleType = scaleType
        this.imageCaptureTargetSize = imageCaptureTargetSize
        this.isImageAnalysisEnabled = isImageAnalysisEnabled
        this.implementationMode = implementationMode
        this.isFocusOnTapEnabled = isFocusOnTapEnabled
        this.flashMode = flashMode
        this.enableTorch = enableTorch
        this.imageCaptureMode = imageCaptureMode
        this.zoomRatio = zoomRatio
        this.exposureCompensation = exposureCompensation
        this.videoQualitySelector = videoQualitySelector
        this.isPinchToZoomEnabled = isPinchToZoomEnabled
    }

    internal fun recoveryState() {
        setTorchEnabled(enableTorch)
    }

    internal fun dispose() {
        captureSession.stopRunning()
        previewLayer = null
    }
}
