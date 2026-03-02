package com.ujizin.camposer.controller.takepicture

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.internal.core.CameraEngine
import com.ujizin.camposer.internal.core.JvmCameraEngine
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.opencv.global.opencv_imgcodecs.imencode
import org.bytedeco.opencv.opencv_core.Mat
import java.io.File

internal actual class DefaultTakePictureCommand private constructor(
  private val cameraEngine: JvmCameraEngine,
) : TakePictureCommand {
  internal constructor(cameraEngine: CameraEngine) : this(
    cameraEngine = cameraEngine as JvmCameraEngine,
  )

  actual override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit) {
    val mat = cameraEngine.currentMat
    if (mat == null || mat.empty()) {
      onImageCaptured(
        CaptureResult.Error(IllegalStateException("No camera frame available")),
      )
      return
    }

    try {
      val bytes = encodeToJpegBytes(mat)
      onImageCaptured(CaptureResult.Success(bytes))
    } catch (e: Exception) {
      onImageCaptured(CaptureResult.Error(e))
    }
  }

  actual override fun takePicture(
    filename: String,
    onImageCaptured: (CaptureResult<String>) -> Unit,
  ) {
    val mat = cameraEngine.currentMat
    if (mat == null || mat.empty()) {
      onImageCaptured(
        CaptureResult.Error(IllegalStateException("No camera frame available")),
      )
      return
    }

    try {
      val bytes = encodeToJpegBytes(mat)
      val file = File(filename)
      file.parentFile?.mkdirs()
      file.writeBytes(bytes)
      onImageCaptured(CaptureResult.Success(filename))
    } catch (e: Exception) {
      onImageCaptured(CaptureResult.Error(e))
    }
  }

  private fun encodeToJpegBytes(mat: Mat): ByteArray {
    val buf = BytePointer()
    val success = imencode(".jpg", mat, buf)
    if (!success) {
      throw IllegalStateException("Failed to encode frame as JPEG")
    }
    val size = buf.limit().toInt()
    val bytes = ByteArray(size)
    buf.asByteBuffer().get(bytes)
    return bytes
  }
}
