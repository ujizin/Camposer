package com.ujizin.camposer.controller.takepicture

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.error.CaptureModeException
import com.ujizin.camposer.internal.command.IOSTakePictureCommand
import com.ujizin.camposer.internal.extensions.toCaptureResult
import com.ujizin.camposer.session.IOSCameraSession
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.flashMode
import platform.AVFoundation.position

internal actual class DefaultTakePictureCommand(
  private val iosCameraSession: IOSCameraSession,
  private val cameraState: CameraState,
  private val takePictureCommand: IOSTakePictureCommand =
    IOSTakePictureCommand(
      captureSession = iosCameraSession.captureSession,
    ),
) : TakePictureCommand {
  private val captureDeviceInput: AVCaptureDeviceInput
    get() = iosCameraSession.captureDeviceInput

  private val currentPosition: AVCaptureDevicePosition
    get() = captureDeviceInput.device.position

  actual override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit) {
    if (cameraState.captureMode != CaptureMode.Image) {
      onImageCaptured(
        CaptureResult.Error(CaptureModeException(CaptureMode.Image)),
      )
      return
    }

    takePictureCommand(
      isMirrorEnabled = cameraState.mirrorMode.isMirrorEnabled(currentPosition),
      flashMode = cameraState.flashMode.mode,
      videoOrientation = cameraState.getCurrentVideoOrientation(),
      onPictureCaptured = onPictureCaptured(onImageCaptured),
    )
  }

  actual override fun takePicture(
    filename: String,
    onImageCaptured: (CaptureResult<String>) -> Unit,
  ) {
    if (cameraState.captureMode != CaptureMode.Image) {
      onImageCaptured(
        CaptureResult.Error(CaptureModeException(CaptureMode.Image)),
      )
      return
    }

    takePictureCommand(
      filename = filename,
      isMirrorEnabled = cameraState.mirrorMode.isMirrorEnabled(currentPosition),
      flashMode = captureDeviceInput.device.flashMode,
      videoOrientation = cameraState.getCurrentVideoOrientation(),
      onPictureCaptured = onPictureCaptured(onImageCaptured),
    )
  }

  private fun <T> onPictureCaptured(
    onImageCaptured: (CaptureResult<T>) -> Unit,
  ): (Result<T>) -> Unit =
    { result ->
      onImageCaptured(result.toCaptureResult())

      // iOS disables the torch when flash mode is on, so the torch must be re-enabled.
      if (cameraState.isTorchEnabled && cameraState.flashMode == FlashMode.On) {
        iosCameraSession.setTorchEnabled(true)
      }
    }
}
