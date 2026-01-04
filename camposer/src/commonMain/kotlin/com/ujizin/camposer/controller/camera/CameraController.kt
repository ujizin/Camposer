package com.ujizin.camposer.controller.camera

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.MirrorMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import kotlinx.coroutines.flow.StateFlow

/**
 * A controller that manages the state and interactions of the camera.
 *
 * This class serves as the primary interface for interacting with the underlying camera when bound
 * to a [com.ujizin.camposer.session.CameraSession].
 *
 * It offers methods to capture images, record videos, and manipulate camera parameters such as
 * zoom, exposure, focus, flash modes, and torch settings.
 */
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

  override fun resumeRecording(): Result<Boolean>

  override fun pauseRecording(): Result<Boolean>

  override fun stopRecording(): Result<Boolean>

  override fun muteRecording(isMuted: Boolean): Result<Boolean>

  override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit)

  override fun takePicture(
    filename: String,
    onImageCaptured: (CaptureResult<String>) -> Unit,
  )

  override fun setZoomRatio(zoomRatio: Float)

  override fun setExposureCompensation(exposureCompensation: Float)

  override fun setOrientationStrategy(strategy: OrientationStrategy)

  override fun setMirrorMode(mirrorMode: MirrorMode)

  override fun setFlashMode(flashMode: FlashMode): Result<Unit>

  override fun setTorchEnabled(isTorchEnabled: Boolean): Result<Unit>

  override fun setVideoFrameRate(frameRate: Int): Result<Unit>

  override fun setVideoStabilizationEnabled(mode: VideoStabilizationMode): Result<Unit>
}
