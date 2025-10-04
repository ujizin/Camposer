package com.ujizin.camposer.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.command.DefaultTakePictureCommand
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.extensions.withConfigurationLock
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.session.IOSCameraSession
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.setExposureTargetBias
import platform.AVFoundation.videoZoomFactor
import platform.CoreGraphics.CGPoint
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
public actual class CameraState internal constructor(
    internal val controller: CameraController,
    public val iosCameraSession: IOSCameraSession = IOSCameraSession(),
    public actual val info: CameraInfo = CameraInfo(iosCameraSession)
) {

    internal actual var camSelector: CamSelector = CamSelector.Back
        set(value) {
            if (value == field) return
            field = value
            iosCameraSession.setCameraPosition(position = value.position)
            rebindCamera()
        }

    internal actual var captureMode: CaptureMode = CaptureMode.Image
        set(value) {
            if (value == field) return
            val previousMode = field
            field = value
            iosCameraSession.switchCameraOutput(previousMode.output, value.output)
            onCaptureModeChanged()
        }

    internal actual var resolutionPreset: ResolutionPreset = ResolutionPreset.Default
        set(value) {
            if (field == value) return
            field = value
            iosCameraSession.setCameraPreset(value.presets.toList())
        }

    internal actual var imageCaptureMode: ImageCaptureMode = ImageCaptureMode.Balanced
        set(value) {
            if (value == field) return
            field = value
            iosCameraSession.setCameraOutputQuality(
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
            iosCameraSession.setFlashMode(value.mode)
        }

    internal actual var scaleType: ScaleType = ScaleType.FillCenter
        set(value) {
            if (value == field) return
            field = value
            iosCameraSession.previewLayer?.videoGravity = value.gravity
        }

    // No-op in iOS
    internal actual var implementationMode: ImplementationMode = ImplementationMode.Performance

    internal actual var isImageAnalyzerEnabled: Boolean = false
        set(value) {
            field = value
            imageAnalyzer?.isEnabled = isImageAnalyzerEnabled
        }

    internal actual var imageAnalyzer: ImageAnalyzer? = null
        set(value) {
            if (value == null || field == value) return
            value.isEnabled = isImageAnalyzerEnabled
            field = value
        }

    internal actual var isFocusOnTapEnabled: Boolean = true

    internal actual var enableTorch: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            iosCameraSession.setTorchEnabled(value)
        }

    internal var zoomRatio: Float
        get() = iosCameraSession.device.videoZoomFactor.toFloat()
        set(value) {
            if (zoomRatio == value || value !in info.minZoom..info.maxZoom) return
            iosCameraSession.device.withConfigurationLock {
                videoZoomFactor = value.toDouble()
            }
        }

    public actual var exposureCompensation: Float? = null
        private set(value) {
            if (value == null || field == value) return
            field = value
            iosCameraSession.device.withConfigurationLock {
                setExposureTargetBias(value, {})
            }
        }

    public actual var isInitialized: Boolean = false
        get() = iosCameraSession.isRunning
        private set

    public actual var isMuted: Boolean by mutableStateOf(false)
        private set

    public actual var isPinchToZoomEnabled: Boolean by mutableStateOf(false)

    public actual var isStreaming: Boolean = false
        get() = isInitialized

    init {
        setupCamera()
    }

    private fun setupCamera() = with(iosCameraSession) {
        setCameraPosition(position = camSelector.position)
        rebindCamera()
        controller.initialize(
            recordController = DefaultRecordController(
                cameraManager = iosCameraSession,
                captureModeProvider = { captureMode }
            ),
            takePictureCommand = DefaultTakePictureCommand(
                cameraManager = iosCameraSession,
                captureModeProvider = { captureMode },
            )
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    internal fun startCamera(view: UIView) = iosCameraSession.start(
        view = view,
        output = captureMode.output,
        position = camSelector.position,
        gravity = scaleType.gravity,
        isMuted = isMuted,
        presets = resolutionPreset.presets.toList(),
    )

    internal fun renderCamera(view: UIView) = iosCameraSession.renderPreviewLayer(view)

    internal fun setFocusPoint(focusPoint: CValue<CGPoint>) =
        iosCameraSession.setFocusPoint(focusPoint)

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
        this.isImageAnalyzerEnabled = isImageAnalysisEnabled
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

    private fun rebindCamera() = with(iosCameraSession.device) {
        info.rebind(captureMode.output)
    }

    private fun onCaptureModeChanged() {
        rebindCamera()
        iosCameraSession.setCameraOutputQuality(
            quality = imageCaptureMode.strategy,
            highResolutionEnabled = imageCaptureMode.highResolutionEnabled,
        )
    }

    internal fun recoveryState() {
        iosCameraSession.setTorchEnabled(enableTorch)
    }

    internal fun dispose() {
        iosCameraSession.release()
    }
}
