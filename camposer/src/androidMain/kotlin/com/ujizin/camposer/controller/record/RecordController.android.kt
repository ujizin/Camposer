package com.ujizin.camposer.controller.record

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.camera.video.FileDescriptorOutputOptions
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.video.AudioConfig
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.util.Consumer
import com.ujizin.camposer.CaptureResult
import java.io.File
import java.util.concurrent.Executor


internal actual class DefaultRecordController(
    private val cameraController: CameraController,
    private val mainExecutor: Executor,
) : AndroidRecordController {

    private var recordController: Recording? = null

    actual override var isMuted: Boolean by mutableStateOf(false)
    actual override var isRecording: Boolean by mutableStateOf(false)

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    actual override fun startRecording(
        filename: String,
        onVideoCaptured: (CaptureResult<String>) -> Unit,
    ) {
        val file = File(filename)
        startRecording(
            fileOutputOptions = FileOutputOptions.Builder(file).build(),
            onResult = { result ->
                val result = when (result) {
                    is CaptureResult.Error -> CaptureResult.Error(result.throwable)
                    is CaptureResult.Success<Uri?> -> CaptureResult.Success(file.absolutePath)
                }
                onVideoCaptured(result)
            }
        )
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun startRecording(
        fileDescriptorOutputOptions: FileDescriptorOutputOptions,
        audioConfig: AudioConfig,
        onResult: (CaptureResult<Uri?>) -> Unit,
    ) = prepareRecording(onResult) {
        isMuted = !audioConfig.audioEnabled
        cameraController.startRecording(
            fileDescriptorOutputOptions,
            audioConfig,
            mainExecutor,
            getConsumerEvent(onResult)
        )
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun startRecording(
        fileOutputOptions: FileOutputOptions,
        audioConfig: AudioConfig,
        onResult: (CaptureResult<Uri?>) -> Unit,
    ): Unit = prepareRecording(onResult) {
        isMuted = !audioConfig.audioEnabled
        cameraController.startRecording(
            fileOutputOptions,
            audioConfig,
            mainExecutor,
            getConsumerEvent(onResult)
        )
    }

    override fun startRecording(
        mediaStoreOutputOptions: MediaStoreOutputOptions,
        audioConfig: AudioConfig,
        onResult: (CaptureResult<Uri?>) -> Unit,
    ) = prepareRecording(onError = onResult) {
        isMuted = !audioConfig.audioEnabled
        cameraController.startRecording(
            mediaStoreOutputOptions,
            audioConfig,
            mainExecutor,
            getConsumerEvent(onResult)
        )
    }

    actual override fun resumeRecording() {
        recordController?.resume()
    }

    actual override fun pauseRecording() {
        recordController?.pause()
    }

    actual override fun stopRecording() {
        recordController?.stop()
    }

    actual override fun muteRecording(isMuted: Boolean) {
        recordController?.mute(isMuted)
        this.isMuted = isMuted
    }


    private fun prepareRecording(
        onError: (CaptureResult.Error) -> Unit,
        onRecordBuild: () -> Recording,
    ) {
        try {
            isRecording = true
            recordController = onRecordBuild()
        } catch (exception: Exception) {
            isRecording = false
            onError(CaptureResult.Error(exception))
        }
    }

    private fun getConsumerEvent(
        onResult: (CaptureResult<Uri?>) -> Unit,
    ): Consumer<VideoRecordEvent> = Consumer { event ->
        if (event is VideoRecordEvent.Finalize) {
            isRecording = false
            isMuted = false
            val result = when {
                !event.hasError() -> CaptureResult.Success(event.outputResults.outputUri)
                else -> CaptureResult.Error(
                    Exception("Video error code: ${event.error}, cause: ${event.cause}"),
                )
            }
            recordController = null
            onResult(result)
        }
    }

}