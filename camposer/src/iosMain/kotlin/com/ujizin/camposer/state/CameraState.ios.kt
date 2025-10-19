package com.ujizin.camposer.state

import com.ujizin.camposer.command.DefaultTakePictureCommand
import com.ujizin.camposer.config.CameraConfig
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.session.IOSCameraSession
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureSession
import platform.CoreGraphics.CGPoint
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
public actual class CameraState internal constructor(
    internal val controller: CameraController,
    public val captureSession: AVCaptureSession = AVCaptureSession(),
    public val iosCameraSession: IOSCameraSession = IOSCameraSession(captureSession),
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
                cameraConfig = config,
            ),
            takePictureCommand = DefaultTakePictureCommand(
                iosCameraSession = iosCameraSession,
                cameraConfig = config,
            )
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    internal fun startCamera() = iosCameraSession.start(
        captureOutput = config.captureMode.output,
        position = config.camSelector.position,
        gravity = config.scaleType.gravity,
        isMuted = controller.isMuted,
        presets = config.resolutionPreset.presets.toList(),
    )

    internal fun renderCamera(view: UIView) = iosCameraSession.renderPreviewLayer(view)

    internal fun setFocusPoint(
        focusPoint: CValue<CGPoint>,
    ) = iosCameraSession.setFocusPoint(focusPoint)

    private fun rebindCamera() = with(iosCameraSession.device) {
        info.rebind(config.captureMode.output)
        config.rebind()
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
