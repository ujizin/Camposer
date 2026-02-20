package com.ujizin.camposer.internal.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.internal.error.CameraNotRunningException
import com.ujizin.camposer.internal.error.ErrorRecordVideoException
import com.ujizin.camposer.internal.error.VideoOutputNotFoundException
import com.ujizin.camposer.internal.extensions.firstIsInstanceOrNull
import com.ujizin.camposer.internal.extensions.setMirrorEnabled
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVMediaTypeVideo
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.darwin.NSObject

internal class IOSRecordController(
  private val cameraController: IOSCameraController,
) {
  private var videoDelegate: AVCaptureFileOutputRecordingDelegateProtocol? = null

  private val captureSession: AVCaptureSession
    get() = cameraController.captureSession
  private val videoRecordOutput: AVCaptureMovieFileOutput?
    get() = captureSession.outputs.firstIsInstanceOrNull<AVCaptureMovieFileOutput>()

  var isMuted: Boolean by mutableStateOf(false)
    private set
  var isRecording: Boolean by mutableStateOf(false)
    private set

  fun start(
    filename: String,
    videoOrientation: AVCaptureVideoOrientation,
    isMirrorEnabled: Boolean,
    onVideoCaptured: (Result<String>) -> Unit,
  ) {
    if (!captureSession.isRunning()) {
      return onVideoCaptured(Result.failure(CameraNotRunningException()))
    }

    val videoRecordOutput = videoRecordOutput
      ?: return onVideoCaptured(Result.failure(VideoOutputNotFoundException()))

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
        onVideoCaptured(result)
        isRecording = false
        videoDelegate = null
      }
    }.apply { videoDelegate = this }

    videoRecordOutput.startRecordingToOutputFileURL(
      outputFileURL = NSURL.fileURLWithPath(filename),
      recordingDelegate = videoDelegate,
    )

    isRecording = true
  }

  fun resume(): Result<Boolean> {
    videoRecordOutput?.resumeRecording() ?: return Result.failure(VideoOutputNotFoundException())
    return Result.success(true)
  }

  fun pause(): Result<Boolean> {
    videoRecordOutput?.pauseRecording() ?: return Result.failure(VideoOutputNotFoundException())
    return Result.success(true)
  }

  fun stop(): Result<Boolean> {
    videoRecordOutput?.stopRecording() ?: return Result.failure(VideoOutputNotFoundException())
    isRecording = false
    isMuted = false
    return Result.success(true)
  }

  fun mute(isMuted: Boolean): Result<Boolean> {
    this.isMuted = isMuted
    cameraController.setAudioEnabled(!isMuted)
    return Result.success(true)
  }
}
