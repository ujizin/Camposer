package com.ujizin.camposer.controller.camera

import CameraControllerContract
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.result.CaptureResult
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode
import kotlinx.io.files.Path

public expect class CameraController : CameraControllerContract {

    public constructor()

    override val state: CameraState?
    override val info: CameraInfo?

    override val isMuted: Boolean
    override val isRecording: Boolean

    override fun startRecording(
        path: Path,
        onVideoCaptured: (CaptureResult<Path>) -> Unit,
    )

    override fun resumeRecording()
    override fun pauseRecording()
    override fun stopRecording()
    override fun muteRecording(isMuted: Boolean)
    override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit)
    override fun takePicture(
        path: Path,
        onImageCaptured: (CaptureResult<Path>) -> Unit,
    )

    override fun setZoomRatio(zoomRatio: Float)
    override fun setExposureCompensation(exposureCompensation: Float)
    override fun setFlashMode(flashMode: FlashMode)
    override fun setTorchEnabled(isTorchEnabled: Boolean)
}
