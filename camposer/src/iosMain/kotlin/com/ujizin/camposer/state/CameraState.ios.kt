package com.ujizin.camposer.state

import com.ujizin.camposer.command.DefaultTakePictureCommand
import com.ujizin.camposer.config.CameraConfig
import com.ujizin.camposer.config.update
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.session.IOSCameraSession
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGPoint
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
public actual class CameraState internal constructor(
    internal val controller: CameraController,
    public val iosCameraSession: IOSCameraSession = IOSCameraSession(),
    public actual val info: CameraInfo = CameraInfo(iosCameraSession),
    public actual val config: CameraConfig = CameraConfig(iosCameraSession, info),
) {

    public actual var isInitialized: Boolean = false
        get() = iosCameraSession.isRunning
        private set

    public actual var isStreaming: Boolean = false
        get() = isInitialized

    init {
        setupCamera()
    }

    private fun setupCamera() = with(iosCameraSession) {
        setCameraPosition(position = config.camSelector.position)
        rebindCamera()
        config.rebindCamera = ::rebindCamera
        controller.initialize(
            recordController = DefaultRecordController(
                iosCameraSession = iosCameraSession,
                captureModeProvider = { config.captureMode }
            ),
            takePictureCommand = DefaultTakePictureCommand(
                cameraManager = iosCameraSession,
                captureModeProvider = { config.captureMode },
            )
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    internal fun startCamera(view: UIView) = iosCameraSession.start(
        view = view,
        output = config.captureMode.output,
        position = config.camSelector.position,
        gravity = config.scaleType.gravity,
        isMuted = controller.isMuted,
        presets = config.resolutionPreset.presets.toList(),
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
        isImageAnalysisEnabled: Boolean,
        imageAnalyzer: ImageAnalyzer?,
        implementationMode: ImplementationMode,
        isFocusOnTapEnabled: Boolean,
        flashMode: FlashMode,
        zoomRatio: Float,
        imageCaptureStrategy: ImageCaptureStrategy,
        enableTorch: Boolean,
        exposureCompensation: Float?,
        resolutionPreset: ResolutionPreset,
        isPinchToZoomEnabled: Boolean,
    ) {
        config.update(
            camSelector = camSelector,
            captureMode = captureMode,
            scaleType = scaleType,
            isImageAnalysisEnabled = isImageAnalysisEnabled,
            imageAnalyzer = imageAnalyzer,
            implementationMode = implementationMode,
            isFocusOnTapEnabled = isFocusOnTapEnabled,
            flashMode = flashMode,
            zoomRatio = zoomRatio,
            imageCaptureStrategy = imageCaptureStrategy,
            isTorchEnabled = enableTorch,
            exposureCompensation = exposureCompensation,
            resolutionPreset = resolutionPreset,
            isPinchToZoomEnabled = isPinchToZoomEnabled,
        )
    }

    private fun rebindCamera() = with(iosCameraSession.device) {
        info.rebind(config.captureMode.output)
        iosCameraSession.setCameraOutputQuality(
            quality = config.imageCaptureStrategy.strategy,
            highResolutionEnabled = config.imageCaptureStrategy.highResolutionEnabled,
        )
    }

    internal fun recoveryState() {
        iosCameraSession.setTorchEnabled(config.isTorchEnabled)
    }

    internal fun dispose() {
        iosCameraSession.release()
    }
}
