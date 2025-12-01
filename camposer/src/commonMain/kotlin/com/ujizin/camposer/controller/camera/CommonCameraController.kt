package com.ujizin.camposer.controller.camera

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.controller.takepicture.TakePictureCommand
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.utils.Bundle
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Common Camera Controller that abstracts the implementation of Common API.
 *
 * This class manages the coordination between recording video, taking pictures, and managing camera state.
 * It handles the lifecycle of camera operations, ensuring that commands are queued or executed based on
 * whether the camera session is currently running in common module.
 *
 * This class is intended to be used internally. Please use [CameraController] instead.
 */
public abstract class CommonCameraController<
  RC : RecordController,
  TPC : TakePictureCommand,
> internal constructor() : CameraControllerContract {
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
  ): Unit = recordController.runBind { startRecording(filename, onVideoCaptured) }

  override fun resumeRecording(): Unit = recordController.runBind { resumeRecording() }

  override fun pauseRecording(): Unit = recordController.runBind { pauseRecording() }

  override fun stopRecording(): Unit = recordController.runBind { stopRecording() }

  override fun muteRecording(isMuted: Boolean): Unit =
    recordController.runBind {
      muteRecording(isMuted)
    }

  override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit): Unit =
    takePictureCommand.runBind { takePicture(onImageCaptured) }

  override fun takePicture(
    filename: String,
    onImageCaptured: (CaptureResult<String>) -> Unit,
  ): Unit = takePictureCommand.runBind { takePicture(filename, onImageCaptured) }

  override fun setZoomRatio(zoomRatio: Float): Unit =
    state.runBind {
      if (!isRunning.value) {
        pendingBundle[ZOOM_KEY] = zoomRatio
        return@runBind
      }

      this.zoomRatio = zoomRatio
    }

  override fun setExposureCompensation(exposureCompensation: Float): Unit =
    state.runBind {
      if (!isRunning.value) {
        pendingBundle[EXPOSURE_COMPENSATION_KEY] = exposureCompensation
        return@runBind
      }
      this.exposureCompensation = exposureCompensation
    }

  override fun setOrientationStrategy(strategy: OrientationStrategy) {
    state.runBind { orientationStrategy = strategy }
  }

  override fun setFlashMode(flashMode: FlashMode): Result<Unit> =
    runCatching {
      state.runBind {
        if (!isRunning.value) {
          pendingBundle[FLASH_MODE_KEY] = flashMode
          return@runBind
        }
        this.flashMode = flashMode
      }
    }

  override fun setTorchEnabled(isTorchEnabled: Boolean): Result<Unit> =
    runCatching {
      state.runBind {
        if (!isRunning.value) {
          pendingBundle[TORCH_KEY] = isTorchEnabled
          return@runBind
        }
        this.isTorchEnabled = isTorchEnabled
      }
    }

  override fun setVideoFrameRate(frameRate: Int): Result<Unit> =
    runCatching {
      state.runBind {
        if (!isRunning.value) {
          pendingBundle[FRAME_RATE_KEY] = frameRate
          return@runBind
        }

        this.frameRate = frameRate
      }
    }

  override fun setVideoStabilizationEnabled(mode: VideoStabilizationMode): Result<Unit> =
    runCatching {
      state.runBind {
        if (!isRunning.value) {
          pendingBundle[VIDEO_STABILIZATION_KEY] = mode
          return@runBind
        }

        this.videoStabilizationMode = mode
      }
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
    if (_isRunning.value) return

    _isRunning.update { true }

    pendingBundle.get<Float>(ZOOM_KEY)?.let(::setZoomRatio)
    pendingBundle.get<Float>(EXPOSURE_COMPENSATION_KEY)?.let(::setExposureCompensation)
    pendingBundle.get<Boolean>(TORCH_KEY)?.let(::setTorchEnabled)
    pendingBundle.get<FlashMode>(FLASH_MODE_KEY)?.let(::setFlashMode)
    pendingBundle.get<Int>(FRAME_RATE_KEY)?.let(::setVideoFrameRate)
    pendingBundle
      .get<VideoStabilizationMode>(VIDEO_STABILIZATION_KEY)
      ?.let(::setVideoStabilizationEnabled)

    pendingBundle.clear()
  }

  protected fun <T, R> T?.runBind(block: T.() -> R): R {
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
    private const val FRAME_RATE_KEY = "frame_rate_key"
    private const val VIDEO_STABILIZATION_KEY = "video_stabilization_key"
  }
}
