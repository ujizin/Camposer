package com.ujizin.camposer.controller.camera

import CameraControllerContract
import com.ujizin.camposer.command.TakePictureCommand
import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.result.CaptureResult
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.utils.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

public abstract class CommonCameraController<RC : RecordController, TPC : TakePictureCommand> :
    CameraControllerContract {

    protected var recordController: RC? = null
        private set

    protected var takePictureCommand: TPC? = null
        private set

    private var cameraState: CameraState? = null
    override val state: CameraState?
        get() = cameraState

    private var cameraInfo: CameraInfo? = null
    override val info: CameraInfo?
        get() = cameraInfo

    private val _isRunning = MutableStateFlow(false)
    public override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val pendingBundle = Bundle()

    override val isMuted: Boolean
        get() = recordController?.isMuted ?: false

    override val isRecording: Boolean
        get() = recordController?.isRecording ?: false

    override fun startRecording(
        filename: String,
        onVideoCaptured: (CaptureResult<String>) -> Unit,
    ): Unit = recordController.bindRun { startRecording(filename, onVideoCaptured) }

    override fun resumeRecording(): Unit = recordController.bindRun { resumeRecording() }

    override fun pauseRecording(): Unit = recordController.bindRun { pauseRecording() }

    override fun stopRecording(): Unit = recordController.bindRun { stopRecording() }

    override fun muteRecording(isMuted: Boolean): Unit = recordController.bindRun {
        muteRecording(isMuted)
    }

    override fun takePicture(
        onImageCaptured: (CaptureResult<ByteArray>) -> Unit,
    ): Unit = takePictureCommand.bindRun { takePicture(onImageCaptured) }

    override fun takePicture(
        filename: String,
        onImageCaptured: (CaptureResult<String>) -> Unit,
    ): Unit = takePictureCommand.bindRun { takePicture(filename, onImageCaptured) }

    override fun setZoomRatio(zoomRatio: Float): Unit = state.bindRun {
        if (!isRunning.value) {
            pendingBundle[ZOOM_KEY] = zoomRatio
            return@bindRun
        }

        this.zoomRatio = zoomRatio
    }

    override fun setExposureCompensation(exposureCompensation: Float): Unit = state.bindRun {
        if (!isRunning.value) {
            pendingBundle[EXPOSURE_COMPENSATION_KEY] = exposureCompensation
            return@bindRun
        }
        this.exposureCompensation = exposureCompensation
    }

    override fun setFlashMode(flashMode: FlashMode): Unit = state.bindRun {
        if (!isRunning.value) {
            pendingBundle[FLASH_MODE_KEY] = flashMode
            return@bindRun
        }
        this.flashMode = flashMode
    }

    override fun setTorchEnabled(isTorchEnabled: Boolean): Unit = state.bindRun {
        if (!isRunning.value) {
            pendingBundle[TORCH_KEY] = isTorchEnabled
            return@bindRun
        }
        this.isTorchEnabled = isTorchEnabled
    }

    internal fun initialize(
        recordController: RC,
        takePictureCommand: TPC,
        cameraState: CameraState,
        cameraInfo: CameraInfo,
    ) {
        this.recordController = recordController
        this.takePictureCommand = takePictureCommand
        this.cameraState = cameraState
        this.cameraInfo = cameraInfo
    }

    internal fun onSessionStarted() {
        _isRunning.update { true }

        pendingBundle.get<Float>(ZOOM_KEY)?.let(::setZoomRatio)
        pendingBundle.get<Float>(EXPOSURE_COMPENSATION_KEY)?.let(::setExposureCompensation)
        pendingBundle.get<Boolean>(TORCH_KEY)?.let(::setTorchEnabled)
        pendingBundle.get<FlashMode>(FLASH_MODE_KEY)?.let(::setFlashMode)
        pendingBundle.clear()
    }

    protected fun <T, R> T?.bindRun(block: T.() -> R): R {
        require(this != null) {
            "CameraController must be bind to cameraSession first to be used!"
        }

        return block()
    }

    internal companion object {
        private const val ZOOM_KEY = "zoom_key"
        private const val EXPOSURE_COMPENSATION_KEY = "exposure_compensation_key"
        private const val TORCH_KEY = "torch_key"
        private const val FLASH_MODE_KEY = "flash_mode_key"
    }
}
