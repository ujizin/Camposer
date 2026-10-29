package com.ujizin.camposer.controller.takepicture

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.error.CaptureModeException
import com.ujizin.camposer.internal.core.CameraEngine
import com.ujizin.camposer.internal.core.JvmCameraEngine
import com.ujizin.camposer.internal.core.jvm.JvmCameraController
import com.ujizin.camposer.internal.extensions.toCaptureResult
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode

internal actual class DefaultTakePictureCommand private constructor(
  private val cameraEngine: JvmCameraEngine,
) : TakePictureCommand {
  internal constructor(cameraEngine: CameraEngine) : this(cameraEngine as JvmCameraEngine)

  private val controller: JvmCameraController
    get() = cameraEngine.jvmCameraController

  private val cameraState: CameraState
    get() = cameraEngine.cameraState

  actual override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit) {
    if (cameraState.captureMode.value != CaptureMode.Image) {
      onImageCaptured(CaptureResult.Error(CaptureModeException(CaptureMode.Image)))
      return
    }

    controller.takePicture(
      isMirrored = cameraEngine.isMirrorEnabled(),
      onPictureCaptured = { result -> onImageCaptured(result.toCaptureResult()) },
    )
  }

  actual override fun takePicture(
    filename: String,
    onImageCaptured: (CaptureResult<String>) -> Unit,
  ) {
    if (cameraState.captureMode.value != CaptureMode.Image) {
      onImageCaptured(CaptureResult.Error(CaptureModeException(CaptureMode.Image)))
      return
    }

    controller.takePicture(
      filename = filename,
      isMirrored = cameraEngine.isMirrorEnabled(),
      onPictureCaptured = { result -> onImageCaptured(result.toCaptureResult()) },
    )
  }
}
