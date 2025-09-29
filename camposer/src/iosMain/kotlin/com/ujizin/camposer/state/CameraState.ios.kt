package com.ujizin.camposer.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.command.DefaultTakePictureCommand
import com.ujizin.camposer.command.TakePictureCommand
import com.ujizin.camposer.controller.CameraController
import com.ujizin.camposer.controller.IOSCameraManager
import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.extensions.withConfigurationLock
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
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
    public val controller: CameraController,
    public val cameraManager: IOSCameraManager = IOSCameraManager(),
) : RecordController by controller, TakePictureCommand by controller {

    internal actual var camSelector: CamSelector = CamSelector.Back
        set(value) {
            if (value == field) return
            field = value
            cameraManager.setCameraPosition(position = value.position)
            rebindCamera()
        }

    internal actual var captureMode: CaptureMode = CaptureMode.Image
        set(value) {
            if (value == field) return
            val previousMode = field
            field = value
            cameraManager.switchCameraOutput(previousMode.output, value.output)
            onCaptureModeChanged()
        }

    internal actual var resolutionPreset: ResolutionPreset = ResolutionPreset.Default
        set(value) {
            if (field == value) return
            field = value
            cameraManager.setCameraPreset(value.presets.toList())
        }

    internal actual var imageCaptureMode: ImageCaptureMode = ImageCaptureMode.Balanced
        set(value) {
            if (value == field) return
            field = value
            cameraManager.setCameraOutputQuality(
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
            cameraManager.setFlashMode(value.mode)
        }

    internal actual var scaleType: ScaleType = ScaleType.FillCenter
        set(value) {
            if (value == field) return
            field = value
            cameraManager.previewLayer?.videoGravity = value.gravity
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
            cameraManager.setTorchEnabled(value)
        }

    internal var zoomRatio: Float
        get() = cameraManager.device.videoZoomFactor.toFloat()
        set(value) {
            if (zoomRatio == value || value !in minZoom..maxZoom) return
            cameraManager.device.withConfigurationLock {
                videoZoomFactor = value.toDouble()
            }
        }

    public actual var exposureCompensation: Float? = null
        private set(value) {
            if (value == null || field == value) return
            field = value
            cameraManager.device.withConfigurationLock {
                setExposureTargetBias(value, {})
            }
        }

    public actual val isZoomSupported: Boolean = true

    public actual var maxZoom: Float = 1F
        get() = cameraManager.device.activeFormat.videoMaxZoomFactor.toFloat()
        private set

    public actual var minZoom: Float = 1F
        private set

    public actual var minExposure: Float = 0F
        get() = cameraManager.device.minExposureTargetBias
        private set

    public actual var maxExposure: Float = 0F
        get() = cameraManager.device.maxExposureTargetBias
        private set

    public actual val initialExposure: Float by lazy { exposureCompensation ?: 0F }

    public actual val isExposureSupported: Boolean
        get() = true

    public actual var isFocusOnTapSupported: Boolean = false
        get() = cameraManager.isFocusOnTapSupported
        private set

    public actual var isInitialized: Boolean = false
        get() = cameraManager.isRunning
        private set

    public actual var hasFlashUnit: Boolean by mutableStateOf(false)
        private set

    public actual var hasTorchAvailable: Boolean by mutableStateOf(false)
        private set

    public actual var isMuted: Boolean by mutableStateOf(false)
        private set

    public actual var isPinchToZoomEnabled: Boolean by mutableStateOf(false)

    public actual var isStreaming: Boolean = false
        get() = isInitialized

    init {
        setupCamera()
    }

    private fun setupCamera() = with(cameraManager) {
        setCameraPosition(position = camSelector.position)
        rebindCamera()
        controller.initialize(
            recordController = DefaultRecordController(
                cameraManager = cameraManager,
                captureModeProvider = { captureMode }
            ),
            takePictureCommand = DefaultTakePictureCommand(
                cameraManager = cameraManager,
                captureModeProvider = { captureMode },
            )
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    internal fun startCamera(view: UIView) = cameraManager.start(
        view = view,
        output = captureMode.output,
        position = camSelector.position,
        gravity = scaleType.gravity,
        isMuted = isMuted,
        presets = resolutionPreset.presets.toList(),
    )

    internal fun renderCamera(view: UIView) = cameraManager.renderPreviewLayer(view)

    internal fun setFocusPoint(focusPoint: CValue<CGPoint>) =
        cameraManager.setFocusPoint(focusPoint)

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

    private fun rebindCamera() = with(cameraManager.device) {
        hasFlashUnit = hasFlash && isFlashAvailable()
        hasTorchAvailable = hasTorch && isTorchAvailable()
    }

    private fun onCaptureModeChanged() {
        cameraManager.setCameraOutputQuality(
            quality = imageCaptureMode.strategy,
            highResolutionEnabled = imageCaptureMode.highResolutionEnabled,
        )
    }

    internal fun recoveryState() {
        cameraManager.setTorchEnabled(enableTorch)
    }

    internal fun dispose() {
        cameraManager.release()
    }
}
