package com.ujizin.camposer.controller.takepicture

import com.ujizin.camposer.CaptureResult

/**
 * Controller interface for managing take picture operations.
 *
 * Note: This interface defines the underlying contract for taking picture operations.
 * For standard usage in an application, please use the implementation provided by [com.ujizin.camposer.controller.camera.CameraController].
 */
public interface TakePictureCommand {
  public fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit)

  public fun takePicture(
    filename: String,
    onImageCaptured: (CaptureResult<String>) -> Unit,
  )
}
