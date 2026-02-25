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
import androidx.core.util.Consumer
import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.error.RecordNotInitializedException
import com.ujizin.camposer.internal.core.AndroidCameraEngine
import com.ujizin.camposer.internal.core.CameraEngine
import com.ujizin.camposer.internal.core.camerax.RecordEvent
import com.ujizin.camposer.internal.core.camerax.RecordingWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

internal actual class DefaultRecordController private constructor(
  cameraEngine: AndroidCameraEngine,
) : AndroidRecordController {
  constructor(cameraEngine: CameraEngine) : this(
    cameraEngine = cameraEngine as AndroidCameraEngine,
  )

  private val controller = cameraEngine.cameraXController
  private val mainExecutor = cameraEngine.mainExecutor

  private var recordController: RecordingWrapper? = null

  private val _isMuted = MutableStateFlow(false)
  actual override val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

  private val _isRecording = MutableStateFlow(false)
  actual override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

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
    _isMuted.update { !audioConfig.audioEnabled }
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
      _isMuted.update { !audioConfig.audioEnabled }
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
    _isMuted.update { !audioConfig.audioEnabled }
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
    _isMuted.update { isMuted }
    return Result.success(true)
  }

  private fun prepareRecording(
    onError: (CaptureResult.Error) -> Unit,
    onRecordBuild: () -> RecordingWrapper,
  ) {
    try {
      recordController = onRecordBuild()
    } catch (exception: Exception) {
      _isRecording.update { false }
      onError(CaptureResult.Error(exception))
    }
  }

  private fun getConsumerEvent(onResult: (CaptureResult<Uri?>) -> Unit): Consumer<RecordEvent> =
    Consumer { record ->
      if (record.isStarted) {
        _isRecording.update { true }
      }

      if (record.isFinalized) {
        _isRecording.update { false }
        _isMuted.update { false }
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
