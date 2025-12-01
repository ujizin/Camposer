package com.ujizin.camposer.controller.takepicture

import com.ujizin.camposer.CaptureResult

internal expect class DefaultTakePictureCommand : TakePictureCommand {
  override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit)

  override fun takePicture(
    filename: String,
    onImageCaptured: (CaptureResult<String>) -> Unit,
  )
}
