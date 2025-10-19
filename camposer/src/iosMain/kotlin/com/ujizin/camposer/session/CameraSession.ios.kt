package com.ujizin.camposer.session

import com.ujizin.camposer.command.DefaultTakePictureCommand
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.CameraState
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureSession
import platform.CoreGraphics.CGPoint
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
public actual class CameraSession private constructor(
    internal val controller: CameraController,
    public val captureSession: AVCaptureSession = AVCaptureSession(),
    public val iosCameraSession: IOSCameraSession = IOSCameraSession(captureSession),
    public actual val info: CameraInfo = CameraInfo(iosCameraSession),
    public actual val state: CameraState = CameraState(iosCameraSession, info),
) {

    public constructor(
        controller: CameraController,
        captureSession: AVCaptureSession = AVCaptureSession(),
        iosCameraSession: IOSCameraSession = IOSCameraSession(captureSession),
    ): this(
        controller = controller,
        iosCameraSession = iosCameraSession,
        captureSession = captureSession,
        info = CameraInfo(iosCameraSession),
    )

    public actual var isInitialized: Boolean = false
        get() = iosCameraSession.isRunning
        private set

    public actual var isStreaming: Boolean = false
        get() = isInitialized

    init {
        setupCamera()
    }

    private fun setupCamera() = with(iosCameraSession) {
        setCameraPosition(position = state.camSelector.position)
        rebindCamera()
        state.rebindCamera = ::rebindCamera
        controller.initialize(
            recordController = DefaultRecordController(
                iosCameraSession = iosCameraSession,
                cameraConfig = state,
            ),
            takePictureCommand = DefaultTakePictureCommand(
                iosCameraSession = iosCameraSession,
                cameraConfig = state,
            )
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    internal fun startCamera() = iosCameraSession.start(
        captureOutput = state.captureMode.output,
        position = state.camSelector.position,
        gravity = state.scaleType.gravity,
        isMuted = controller.isMuted,
        presets = state.resolutionPreset.presets.toList(),
    )

    internal fun renderCamera(view: UIView) = iosCameraSession.renderPreviewLayer(view)

    internal fun setFocusPoint(
        focusPoint: CValue<CGPoint>,
    ) = iosCameraSession.setFocusPoint(focusPoint)

    private fun rebindCamera() = with(iosCameraSession.device) {
        info.rebind(state.captureMode.output)
        state.rebind()
        iosCameraSession.setCameraOutputQuality(
            quality = state.imageCaptureStrategy.strategy,
            highResolutionEnabled = state.imageCaptureStrategy.highResolutionEnabled,
        )
    }

    internal fun recoveryState() {
        iosCameraSession.setTorchEnabled(state.isTorchEnabled)
    }

    internal fun dispose() {
        iosCameraSession.release()
    }
}
