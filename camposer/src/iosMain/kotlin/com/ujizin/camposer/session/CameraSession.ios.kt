package com.ujizin.camposer.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.command.DefaultTakePictureCommand
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.manager.PreviewManager
import com.ujizin.camposer.state.CameraState
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureSession
import platform.CoreGraphics.CGPoint
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
public actual class CameraSession private constructor(
    internal val controller: CameraController,
    public val previewManager: PreviewManager,
    public val iosCameraSession: IOSCameraSession,
    public val captureSession: AVCaptureSession = AVCaptureSession(),
    public actual val info: CameraInfo = CameraInfo(iosCameraSession),
    public actual val state: CameraState = CameraState(iosCameraSession, info),
) {

    public constructor(
        controller: CameraController,
        previewManager: PreviewManager = PreviewManager(),
        captureSession: AVCaptureSession = AVCaptureSession(),
        iosCameraSession: IOSCameraSession = IOSCameraSession(captureSession, previewManager),
    ) : this(
        controller = controller,
        previewManager = previewManager,
        iosCameraSession = iosCameraSession,
        captureSession = captureSession,
        info = CameraInfo(iosCameraSession),
    )

    public actual var isInitialized: Boolean = false
        get() = iosCameraSession.isRunning
        private set

    public actual var isStreaming: Boolean by mutableStateOf(false)
        internal set

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
            ),
            cameraState = state,
            cameraInfo = info,
        )
        controller.onSessionStarted()
    }

    @OptIn(ExperimentalForeignApi::class)
    internal fun startCamera() = iosCameraSession.start(
        captureOutput = state.captureMode.output,
        position = state.camSelector.position,
        isMuted = controller.isMuted,
        presets = state.resolutionPreset.presets.toList(),
    )

    internal fun renderCamera(view: UIView) = iosCameraSession.renderPreviewLayer(view = view)

    internal fun setFocusPoint(
        focusPoint: CValue<CGPoint>,
    ) = iosCameraSession.setFocusPoint(focusPoint)

    private fun rebindCamera() = with(iosCameraSession.device) {
        info.rebind(state.captureMode.output)
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

    internal actual fun onCamSelectorWillChange() {
    }

    internal actual fun onCamSelectorDidChange() {
    }

}
