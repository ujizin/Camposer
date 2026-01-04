package com.ujizin.camposer.controller.record

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.camera.video.FileDescriptorOutputOptions
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.view.video.AudioConfig
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.util.Consumer
import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.error.RecordNotInitializedException
import com.ujizin.camposer.internal.core.AndroidCameraManagerInternal
import com.ujizin.camposer.internal.core.CameraManagerInternal
import com.ujizin.camposer.internal.core.camerax.RecordEvent
import com.ujizin.camposer.internal.core.camerax.RecordingWrapper
import java.io.File

internal actual class DefaultRecordController private constructor(
  controllerInternal: AndroidCameraManagerInternal,
) : AndroidRecordController {
  constructor(cameraManager: CameraManagerInternal) : this(
    controllerInternal = cameraManager as AndroidCameraManagerInternal,
  )

  private val controller = controllerInternal.controller
  private val mainExecutor = controllerInternal.mainExecutor

  private var recordController: RecordingWrapper? = null

  actual override var isMuted: Boolean by mutableStateOf(false)
    private set
  actual override var isRecording: Boolean by mutableStateOf(false)
    private set

  @RequiresPermission(Manifest.permission.RECORD_AUDIO)
  actual override fun startRecording(
    filename: String,
    onVideoCaptured: (CaptureResult<String>) -> Unit,
  ) {
    val file = File(filename)
    startRecording(
      fileOutputOptions = FileOutputOptions.Builder(file).build(),
      onResult = { result ->
        val result =
          when (result) {
            is CaptureResult.Error -> CaptureResult.Error(result.throwable)
            is CaptureResult.Success<Uri?> -> CaptureResult.Success(file.absolutePath)
          }
        onVideoCaptured(result)
      },
    )
  }

  @RequiresApi(Build.VERSION_CODES.O)
  override fun startRecording(
    fileDescriptorOutputOptions: FileDescriptorOutputOptions,
    audioConfig: AudioConfig,
    onResult: (CaptureResult<Uri?>) -> Unit,
  ) = prepareRecording(onResult) {
    isMuted = !audioConfig.audioEnabled
    controller.startRecording(
      fileDescriptorOutputOptions,
      audioConfig,
      mainExecutor,
      getConsumerEvent(onResult),
    )
  }

  @RequiresPermission(Manifest.permission.RECORD_AUDIO)
  override fun startRecording(
    fileOutputOptions: FileOutputOptions,
    audioConfig: AudioConfig,
    onResult: (CaptureResult<Uri?>) -> Unit,
  ): Unit =
    prepareRecording(onResult) {
      isMuted = !audioConfig.audioEnabled
      controller.startRecording(
        fileOutputOptions,
        audioConfig,
        mainExecutor,
        getConsumerEvent(onResult),
      )
    }

  override fun startRecording(
    mediaStoreOutputOptions: MediaStoreOutputOptions,
    audioConfig: AudioConfig,
    onResult: (CaptureResult<Uri?>) -> Unit,
  ) = prepareRecording(onError = onResult) {
    isMuted = !audioConfig.audioEnabled
    controller.startRecording(
      mediaStoreOutputOptions,
      audioConfig,
      mainExecutor,
      getConsumerEvent(onResult),
    )
  }

  actual override fun resumeRecording(): Result<Boolean> {
    recordController?.resume() ?: return Result.failure(RecordNotInitializedException())
    return Result.success(true)
  }

  actual override fun pauseRecording(): Result<Boolean> {
    recordController?.pause() ?: return Result.failure(RecordNotInitializedException())
    return Result.success(true)
  }

  actual override fun stopRecording(): Result<Boolean> {
    recordController?.stop() ?: return Result.failure(RecordNotInitializedException())
    return Result.success(true)
  }

  actual override fun muteRecording(isMuted: Boolean): Result<Boolean> {
    recordController?.mute(isMuted) ?: return Result.failure(
      RecordNotInitializedException(),
    )
    this.isMuted = isMuted
    return Result.success(true)
  }

  private fun prepareRecording(
    onError: (CaptureResult.Error) -> Unit,
    onRecordBuild: () -> RecordingWrapper,
  ) {
    try {
      isRecording = true
      recordController = onRecordBuild()
    } catch (exception: Exception) {
      isRecording = false
      onError(CaptureResult.Error(exception))
    }
  }

  private fun getConsumerEvent(onResult: (CaptureResult<Uri?>) -> Unit): Consumer<RecordEvent> =
    Consumer { record ->
      if (record.isFinalized) {
        isRecording = false
        isMuted = false
        val result =
          when {
            !record.hasError -> CaptureResult.Success(record.outputUri)

            else -> CaptureResult.Error(
              Exception(
                "Video error code: ${record.error}, cause: ${record.cause}",
              ),
            )
          }
        recordController = null
        onResult(result)
      }
    }
}
