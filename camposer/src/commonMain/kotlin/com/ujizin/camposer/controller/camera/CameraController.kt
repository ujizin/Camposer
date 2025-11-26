package com.ujizin.camposer.controller.camera

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import kotlinx.coroutines.flow.StateFlow

public expect class CameraController : CameraControllerContract {

    public constructor()

    override val state: CameraState?
    override val info: CameraInfo?
    override val isRunning: StateFlow<Boolean>

    override val isMuted: Boolean
    override val isRecording: Boolean

    override fun startRecording(
        filename: String,
        onVideoCaptured: (CaptureResult<String>) -> Unit,
    )

    override fun resumeRecording()
    override fun pauseRecording()
    override fun stopRecording()
    override fun muteRecording(isMuted: Boolean)
    override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit)
    override fun takePicture(
        filename: String,
        onImageCaptured: (CaptureResult<String>) -> Unit,
    )

    override fun setZoomRatio(zoomRatio: Float)
    override fun setExposureCompensation(exposureCompensation: Float)
    override fun setOrientationStrategy(strategy: OrientationStrategy)
    override fun setFlashMode(flashMode: FlashMode): Result<Unit>
    override fun setTorchEnabled(isTorchEnabled: Boolean): Result<Unit>
    override fun setVideoFrameRate(frameRate: Int): Result<Unit>
    override fun setVideoStabilizationEnabled(mode: VideoStabilizationMode): Result<Unit>
}
