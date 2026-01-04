package com.ujizin.camposer.controller.takepicture

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.error.CaptureModeException
import com.ujizin.camposer.internal.core.CameraEngine
import com.ujizin.camposer.internal.core.IOSCameraEngine
import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.internal.extensions.toCaptureResult
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode

internal actual class DefaultTakePictureCommand private constructor(
  private val cameraEngine: IOSCameraEngine,
) : TakePictureCommand {
  private val cameraState: CameraState
    get() = cameraEngine.cameraState

  private val controller: IOSCameraController
    get() = cameraEngine.iOSCameraController

  internal constructor(cameraEngine: CameraEngine) : this(
    cameraEngine = cameraEngine as IOSCameraEngine,
  )

  actual override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit) {
    if (cameraState.captureMode != CaptureMode.Image) {
      onImageCaptured(
        CaptureResult.Error(CaptureModeException(CaptureMode.Image)),
      )
      return
    }

    controller.takePicture(
      isMirrorEnabled = cameraEngine.isMirrorEnabled(),
      flashMode = cameraState.flashMode.mode,
      videoOrientation = cameraEngine.getCurrentVideoOrientation(),
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

    controller.takePicture(
      filename = filename,
      isMirrorEnabled = cameraEngine.isMirrorEnabled(),
      flashMode = cameraState.flashMode.mode,
      videoOrientation = cameraEngine.getCurrentVideoOrientation(),
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
        cameraEngine.setTorchEnabled(true)
      }
    }
}
