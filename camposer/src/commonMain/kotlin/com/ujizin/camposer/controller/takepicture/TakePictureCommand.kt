package com.ujizin.camposer.controller.takepicture

import com.ujizin.camposer.CaptureResult

public interface TakePictureCommand {
  public fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit)

  public fun takePicture(
    filename: String,
    onImageCaptured: (CaptureResult<String>) -> Unit,
  )
}
