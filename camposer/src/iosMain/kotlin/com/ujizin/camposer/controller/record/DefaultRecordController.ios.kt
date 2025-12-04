package com.ujizin.camposer.controller.record

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.error.CaptureModeException
import com.ujizin.camposer.internal.error.CameraNotRunningException
import com.ujizin.camposer.internal.error.ErrorRecordVideoException
import com.ujizin.camposer.internal.error.VideoOutputNotFoundException
import com.ujizin.camposer.internal.extensions.firstIsInstanceOrNull
import com.ujizin.camposer.internal.extensions.setMirrorEnabled
import com.ujizin.camposer.internal.extensions.toCaptureResult
import com.ujizin.camposer.session.IOSCameraSession
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.position
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.darwin.NSObject

internal actual class DefaultRecordController(
  private val iosCameraSession: IOSCameraSession,
  private val cameraState: CameraState,
) : RecordController {
  private val captureSession: AVCaptureSession
    get() = iosCameraSession.captureSession

  private val videoRecordOutput: AVCaptureMovieFileOutput?
    get() = captureSession.outputs.firstIsInstanceOrNull<AVCaptureMovieFileOutput>()

  private val currentPosition: AVCaptureDevicePosition
    get() = iosCameraSession.captureDeviceInput.device.position
  private var videoDelegate: AVCaptureFileOutputRecordingDelegateProtocol? = null

  actual override var isMuted: Boolean by mutableStateOf(false)
  actual override var isRecording: Boolean by mutableStateOf(false)

  actual override fun startRecording(
    filename: String,
    onVideoCaptured: (CaptureResult<String>) -> Unit,
  ) = start(
    isMirrorEnabled = cameraState.mirrorMode.isMirrorEnabled(currentPosition),
    videoOrientation = cameraState.getCurrentVideoOrientation(),
    filename = filename,
    onVideoCapture = { result -> onVideoCaptured(result.toCaptureResult()) },
  ).apply { isRecording = true }

  actual override fun resumeRecording(): Result<Boolean> {
    if (cameraState.captureMode != CaptureMode.Video) {
      return Result.failure(CaptureModeException(CaptureMode.Video))
    }

    videoRecordOutput?.resumeRecording() ?: return Result.failure(VideoOutputNotFoundException())
    return Result.success(true)
  }

  actual override fun pauseRecording(): Result<Boolean> {
    if (cameraState.captureMode != CaptureMode.Video) {
      return Result.failure(CaptureModeException(CaptureMode.Video))
    }

    videoRecordOutput?.pauseRecording() ?: return Result.failure(VideoOutputNotFoundException())
    return Result.success(true)
  }

  actual override fun stopRecording(): Result<Boolean> {
    if (cameraState.captureMode != CaptureMode.Video) {
      return Result.failure(CaptureModeException(CaptureMode.Video))
    }

    videoRecordOutput?.stopRecording() ?: return Result.failure(VideoOutputNotFoundException())
    isRecording = false
    isMuted = false
    return Result.success(true)
  }

  actual override fun muteRecording(isMuted: Boolean): Result<Boolean> {
    this.isMuted = isMuted
    iosCameraSession.setAudioEnabled(!isMuted)
    return Result.success(true)
  }

  fun start(
    filename: String,
    videoOrientation: AVCaptureVideoOrientation,
    isMirrorEnabled: Boolean,
    onVideoCapture: (Result<String>) -> Unit,
  ) {
    if (!captureSession.isRunning()) {
      return onVideoCapture(Result.failure(CameraNotRunningException()))
    }

    val videoRecordOutput = videoRecordOutput
    if (videoRecordOutput == null) {
      return onVideoCapture(Result.failure(VideoOutputNotFoundException()))
    }

    videoRecordOutput.setMirrorEnabled(isMirrorEnabled)

    val videoMediaType = videoRecordOutput.connectionWithMediaType(AVMediaTypeVideo)
    videoMediaType?.videoOrientation = videoOrientation

    val videoDelegate = object : NSObject(), AVCaptureFileOutputRecordingDelegateProtocol {
      override fun captureOutput(
        output: AVCaptureFileOutput,
        didFinishRecordingToOutputFileAtURL: NSURL,
        fromConnections: List<*>,
        error: NSError?,
      ) {
        val result = when {
          error != null -> Result.failure(ErrorRecordVideoException(error))
          else -> Result.success(filename)
        }
        onVideoCapture(result)
        videoDelegate = null
      }
    }.apply { videoDelegate = this }

    videoRecordOutput.startRecordingToOutputFileURL(
      outputFileURL = NSURL.Companion.fileURLWithPath(filename),
      recordingDelegate = videoDelegate,
    )
  }
}
