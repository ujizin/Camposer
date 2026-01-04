package com.ujizin.camposer.controller.takepicture

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCapture.OutputFileResults
import androidx.camera.core.ImageCaptureException
import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.internal.core.AndroidCameraEngine
import com.ujizin.camposer.internal.core.CameraEngine
import com.ujizin.camposer.internal.core.camerax.CameraXController
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executor

internal actual class DefaultTakePictureCommand private constructor(
  private val cameraEngine: AndroidCameraEngine,
) : AndroidTakePictureCommand {
  private val controller: CameraXController
    get() = cameraEngine.cameraXController

  private val mainExecutor: Executor
    get() = cameraEngine.mainExecutor
  private val contentResolver: ContentResolver
    get() = cameraEngine.contentResolver

  internal constructor(
    cameraEngine: CameraEngine,
  ) : this(cameraEngine = cameraEngine as AndroidCameraEngine)

  actual override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit) {
    val byteArrayOS = ByteArrayOutputStream()
    takePicture(
      outputFileOptions = OutputFileOptions
        .Builder(byteArrayOS)
        .setMetadata(createMetadata())
        .build(),
      onResult = { result ->
        val result =
          when (result) {
            is CaptureResult.Error -> CaptureResult.Error(result.throwable)
            is CaptureResult.Success<Uri?> -> CaptureResult.Success(byteArrayOS.toByteArray())
          }
        onImageCaptured(result)
      },
    )
  }

  actual override fun takePicture(
    filename: String,
    onImageCaptured: (CaptureResult<String>) -> Unit,
  ): Unit =
    takePicture(
      OutputFileOptions
        .Builder(File(filename))
        .setMetadata(createMetadata())
        .build(),
    ) { androidResult ->
      val result = when (androidResult) {
        is CaptureResult.Error -> CaptureResult.Error(androidResult.throwable)
        is CaptureResult.Success -> CaptureResult.Success(data = filename)
      }
      onImageCaptured(result)
    }

  override fun takePicture(
    contentValues: ContentValues,
    saveCollection: Uri,
    onResult: (CaptureResult<Uri?>) -> Unit,
  ) {
    takePicture(
      outputFileOptions =
        OutputFileOptions
          .Builder(
            contentResolver,
            saveCollection,
            contentValues,
          ).setMetadata(createMetadata())
          .build(),
      onResult = onResult,
    )
  }

  override fun takePicture(
    file: File,
    onResult: (CaptureResult<Uri?>) -> Unit,
  ) {
    takePicture(
      outputFileOptions = OutputFileOptions
        .Builder(file)
        .setMetadata(createMetadata())
        .build(),
      onResult = onResult,
    )
  }

  override fun takePicture(
    outputFileOptions: OutputFileOptions,
    onResult: (CaptureResult<Uri?>) -> Unit,
  ) {
    try {
      controller.takePicture(
        outputFileOptions,
        mainExecutor,
        object : OnImageSavedCallback {
          override fun onImageSaved(outputFileResults: OutputFileResults) {
            onResult(CaptureResult.Success(outputFileResults.savedUri))
          }

          override fun onError(exception: ImageCaptureException) {
            onResult(CaptureResult.Error(exception))
          }
        },
      )
    } catch (exception: Exception) {
      onResult(CaptureResult.Error(exception))
    }
  }

  fun createMetadata() =
    ImageCapture.Metadata().apply {
      this.isReversedHorizontal = cameraEngine.isMirrorEnabled()
    }
}
