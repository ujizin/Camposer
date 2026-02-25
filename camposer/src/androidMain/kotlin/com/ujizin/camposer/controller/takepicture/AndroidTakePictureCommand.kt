package com.ujizin.camposer.controller.takepicture

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.camera.core.ImageCapture.OutputFileOptions
import com.ujizin.camposer.CaptureResult
import java.io.File

public interface AndroidTakePictureCommand : TakePictureCommand {
  /**
   * Take a picture with the camera.
   *
   * @param saveCollection Uri collection where the photo will be saved.
   * @param contentValues Content values of the photo.
   * @param onResult Callback called when [CaptureResult<Uri?>] is ready
   */
  public fun takePicture(
    contentValues: ContentValues,
    saveCollection: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
    onResult: (CaptureResult<Uri?>) -> Unit,
  )

  /**
   * Take a picture with the camera.
   *
   * @param file file where the photo will be saved
   * @param onResult Callback called when [CaptureResult<Uri?>] is ready
   */
  public fun takePicture(
    file: File,
    onResult: (CaptureResult<Uri?>) -> Unit,
  )

  /**
   * Take a picture with the camera.
   *
   * @param outputFileOptions Output file options of the photo.
   * @param onResult Callback called when [CaptureResult] is ready
   */
  public fun takePicture(
    outputFileOptions: OutputFileOptions,
    onResult: (CaptureResult<Uri?>) -> Unit,
  )
}
