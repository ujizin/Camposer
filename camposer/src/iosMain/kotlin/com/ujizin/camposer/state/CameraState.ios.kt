package com.ujizin.camposer.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.controller.IOSCameraController
import com.ujizin.camposer.extensions.toCaptureResult
import com.ujizin.camposer.extensions.withConfigurationLock
import com.ujizin.camposer.result.CaptureResult
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.io.files.Path
import platform.AVFoundation.hasFlash
import platform.AVFoundation.hasTorch
import platform.AVFoundation.isFlashAvailable
import platform.AVFoundation.isTorchAvailable
import platform.AVFoundation.maxExposureTargetBias
import platform.AVFoundation.minExposureTargetBias
import platform.AVFoundation.setExposureTargetBias
import platform.AVFoundation.videoZoomFactor
import platform.CoreGraphics.CGPoint
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
public actual class CameraState internal constructor(
    public val controller: IOSCameraController = IOSCameraController(),
) {

    internal actual var camSelector: CamSelector = CamSelector.Back
        set(value) {
            if (value == field) return
            field = value
            controller.setCameraPosition(position = value.position)
            onCameraSwitched()
        }

    internal actual var captureMode: CaptureMode = CaptureMode.Image
        set(value) {
            if (value == field) return
            val previousMode = field
            field = value
            controller.switchCameraOutput(previousMode.output, value.output)
            onCaptureModeChanged()
        }

    internal actual var resolutionPreset: ResolutionPreset = ResolutionPreset.Default
        set(value) {
            if (field == value) return
            field = value
            controller.setCameraPreset(value.presets.toList())
        }

    internal actual var imageCaptureMode: ImageCaptureMode = ImageCaptureMode.Balanced
        set(value) {
            if (value == field) return
            field = value
            controller.setCameraOutputQuality(
                quality = value.strategy,
                highResolutionEnabled = value.highResolutionEnabled
            )
        }

    // no-op
    internal actual var imageCaptureTargetSize: ImageTargetSize? = ImageTargetSize()

    internal actual var flashMode: FlashMode = FlashMode.Off
        set(value) {
            if (field == value) return
            field = value
            controller.setFlashMode(value.mode)
        }

    internal actual var scaleType: ScaleType = ScaleType.FillCenter
        set(value) {
            if (value == field) return
            field = value
            controller.previewLayer?.videoGravity = value.gravity
        }

    // No-op in iOS
    internal actual var implementationMode: ImplementationMode = ImplementationMode.Performance

    internal actual var isImageAnalysisEnabled: Boolean = false
        set(value) {
            field = value
            imageAnalyzer?.isEnabled = isImageAnalysisEnabled
        }

    internal actual var imageAnalyzer: ImageAnalyzer? = null
        set(value) {
            if (value == null || field == value) return
            value.isEnabled = isImageAnalysisEnabled
            field = value
        }

    internal actual var isFocusOnTapEnabled: Boolean = true

    internal actual var enableTorch: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            controller.setTorchEnabled(value)
        }

    internal var zoomRatio: Float
        get() = controller.device.videoZoomFactor.toFloat()
        set(value) {
            if (zoomRatio == value || value !in minZoom..maxZoom) return
            controller.device.withConfigurationLock {
                videoZoomFactor = value.toDouble()
            }
        }

    public actual var exposureCompensation: Float? = null
        private set(value) {
            if (value == null || field == value) return
            field = value
            controller.device.withConfigurationLock {
                setExposureTargetBias(value, {})
            }
        }

    public actual val isZoomSupported: Boolean = true

    public actual var maxZoom: Float = 1F
        get() = controller.device.activeFormat.videoMaxZoomFactor.toFloat()
        private set

    public actual var minZoom: Float = 1F
        private set

    public actual var minExposure: Float = 0F
        get() = controller.device.minExposureTargetBias
        private set

    public actual var maxExposure: Float = 0F
        get() = controller.device.maxExposureTargetBias
        private set

    public actual val initialExposure: Float by lazy { exposureCompensation ?: 0F }

    public actual val isExposureSupported: Boolean
        get() = true

    public actual var isFocusOnTapSupported: Boolean = false
        get() = controller.isFocusOnTapSupported
        private set

    public actual var isInitialized: Boolean = false
        get() = controller.isRunning
        private set

    public actual var hasFlashUnit: Boolean by mutableStateOf(false)
        private set

    public actual var hasTorchAvailable: Boolean by mutableStateOf(false)
        private set

    public actual var isRecording: Boolean by mutableStateOf(false)
        private set

    public actual var isMuted: Boolean by mutableStateOf(false)
        private set

    public actual var isPinchToZoomEnabled: Boolean by mutableStateOf(false)

    public actual var isStreaming: Boolean = false
        get() = isInitialized

    init {
        prepareCamera()
    }

    private fun prepareCamera() = with(controller) {
        setCameraPosition(position = camSelector.position)
        onCameraSwitched()
    }

    @OptIn(ExperimentalForeignApi::class)
    internal fun startCamera(view: UIView) = controller.start(
        view = view,
        output = captureMode.output,
        position = camSelector.position,
        gravity = scaleType.gravity,
        isMuted = isMuted,
        presets = resolutionPreset.presets.toList(),
    )

    internal fun renderCamera(view: UIView) = controller.renderPreviewLayer(view)

    internal fun setFocusPoint(focusPoint: CValue<CGPoint>) = controller.setFocusPoint(focusPoint)

    public actual fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit) {
        require(captureMode == CaptureMode.Image) { "Capture mode must be CaptureMode.Image" }
        controller.takePicture { result -> onImageCaptured(result.toCaptureResult()) }
    }

    public actual fun takePicture(path: Path, onImageCaptured: (CaptureResult<Path>) -> Unit) {
        require(captureMode == CaptureMode.Image) { "Capture mode must be CaptureMode.Image" }
        controller.takePicture(path) { result -> onImageCaptured(result.toCaptureResult()) }
    }

    public actual fun startRecording(path: Path, onVideoCaptured: (CaptureResult<Path>) -> Unit) {
        require(captureMode == CaptureMode.Video) { "Capture mode must be CaptureMode.Video" }
        controller.startRecording(path) { result -> onVideoCaptured(result.toCaptureResult()) }
        isRecording = true
    }

    public actual fun resumeRecording() {
        require(captureMode == CaptureMode.Video) { "Capture mode must be CaptureMode.Video" }
        controller.resumeRecording()
    }

    public actual fun pauseRecording() {
        require(captureMode == CaptureMode.Video) { "Capture mode must be CaptureMode.Video" }
        controller.pauseRecording()
    }

    public actual fun stopRecording() {
        require(captureMode == CaptureMode.Video) { "Capture mode must be CaptureMode.Video" }
        controller.stopRecording()
        isRecording = false
    }

    public actual fun muteRecording(isMuted: Boolean) {
        controller.setAudioEnabled(!isMuted)
        this.isMuted = isMuted
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
        exposureCompensation: Float?,
        resolutionPreset: ResolutionPreset,
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
        this.resolutionPreset = resolutionPreset
        this.exposureCompensation = exposureCompensation
        this.isPinchToZoomEnabled = isPinchToZoomEnabled
        this.imageAnalyzer = imageAnalyzer
    }

    private fun onCameraSwitched() = with(controller.device) {
        hasFlashUnit = hasFlash && isFlashAvailable()
        hasTorchAvailable = hasTorch && isTorchAvailable()
    }

    private fun onCaptureModeChanged() {
        controller.setCameraOutputQuality(
            quality = imageCaptureMode.strategy,
            highResolutionEnabled = imageCaptureMode.highResolutionEnabled,
        )
    }

    internal fun recoveryState() {
        controller.setTorchEnabled(enableTorch)
    }

    internal fun dispose() {
        controller.release()
    }
}
