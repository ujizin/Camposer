package com.ujizin.camposer.controller.camera

import android.Manifest
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.camera.core.ImageCapture
import androidx.camera.video.FileDescriptorOutputOptions
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.view.video.AudioConfig
import androidx.compose.runtime.Stable
import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.annotation.InternalCamposerApi
import com.ujizin.camposer.controller.record.AndroidRecordController
import com.ujizin.camposer.controller.takepicture.AndroidTakePictureCommand
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.File

@Stable
@OptIn(InternalCamposerApi::class)
public actual class CameraController internal actual constructor(
  dispatcher: CoroutineDispatcher,
) : CommonCameraController(dispatcher),
  AndroidRecordController,
  AndroidTakePictureCommand {
  public actual constructor() : this(Dispatchers.Main)

  /**
   * The Android-typed record controller.
   *
   * Safe: [CommonCameraController.initialize] is internal and its only Android caller is
   * `CameraSession.android.kt`, which always passes a `DefaultRecordController` — an
   * [AndroidRecordController].
   */
  private val androidRecordController: AndroidRecordController?
    get() = recordController as AndroidRecordController?

  /**
   * The Android-typed take picture command. Safe for the same reason as
   * [androidRecordController].
   */
  private val androidTakePictureCommand: AndroidTakePictureCommand?
    get() = takePictureCommand as AndroidTakePictureCommand?

  @RequiresPermission(Manifest.permission.RECORD_AUDIO)
  override fun startRecording(
    fileOutputOptions: FileOutputOptions,
    audioConfig: AudioConfig,
    onResult: (CaptureResult<Uri?>) -> Unit,
  ): Unit =
    androidRecordController.runBind {
      startRecording(
        fileOutputOptions = fileOutputOptions,
        audioConfig = audioConfig,
        onResult = onResult,
      )
    }

  @RequiresApi(Build.VERSION_CODES.O)
  @RequiresPermission(Manifest.permission.RECORD_AUDIO)
  override fun startRecording(
    fileDescriptorOutputOptions: FileDescriptorOutputOptions,
    audioConfig: AudioConfig,
    onResult: (CaptureResult<Uri?>) -> Unit,
  ): Unit =
    androidRecordController.runBind {
      startRecording(
        fileDescriptorOutputOptions = fileDescriptorOutputOptions,
        audioConfig = audioConfig,
        onResult = onResult,
      )
    }

  @RequiresPermission(Manifest.permission.RECORD_AUDIO)
  override fun startRecording(
    mediaStoreOutputOptions: MediaStoreOutputOptions,
    audioConfig: AudioConfig,
    onResult: (CaptureResult<Uri?>) -> Unit,
  ): Unit =
    androidRecordController.runBind {
      startRecording(
        mediaStoreOutputOptions = mediaStoreOutputOptions,
        audioConfig = audioConfig,
        onResult = onResult,
      )
    }

  override fun takePicture(
    contentValues: ContentValues,
    saveCollection: Uri,
    onResult: (CaptureResult<Uri?>) -> Unit,
  ): Unit =
    androidTakePictureCommand.runBind {
      takePicture(
        contentValues = contentValues,
        saveCollection = saveCollection,
        onResult = onResult,
      )
    }

  override fun takePicture(
    file: File,
    onResult: (CaptureResult<Uri?>) -> Unit,
  ): Unit =
    androidTakePictureCommand.runBind {
      takePicture(
        file = file,
        onResult = onResult,
      )
    }

  override fun takePicture(
    outputFileOptions: ImageCapture.OutputFileOptions,
    onResult: (CaptureResult<Uri?>) -> Unit,
  ): Unit =
    androidTakePictureCommand.runBind {
      takePicture(
        outputFileOptions = outputFileOptions,
        onResult = onResult,
      )
    }
}
