package com.ujizin.camposer.extensions

import android.Manifest
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.camera.core.ImageCapture
import androidx.camera.video.FileDescriptorOutputOptions
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.view.video.AudioConfig
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageCaptureResult
import com.ujizin.camposer.state.VideoCaptureResult
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Transform takePicture file to suspend function
 * */
public suspend fun CameraState.takePicture(file: File): Uri? = suspendCancellableCoroutine { cont ->
    with(cont) { takePicture(file, ::takePictureContinuation) }
}

/**
 * Transform takePicture content values to suspend function
 * */
public suspend fun CameraState.takePicture(
    contentValues: ContentValues,
    saveCollection: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
): Uri? = suspendCancellableCoroutine { cont ->
    with(cont) { takePicture(contentValues, saveCollection, ::takePictureContinuation) }
}

/**
 * Transform takePicture output files options to suspend function
 * */
public suspend fun CameraState.takePicture(
    outputFileOptions: ImageCapture.OutputFileOptions,
): Uri? = suspendCancellableCoroutine { cont ->
    with(cont) { takePicture(outputFileOptions, ::takePictureContinuation) }
}

/**
 * Transform toggle recording file to suspend function
 * */
@RequiresApi(Build.VERSION_CODES.O)
@RequiresPermission(Manifest.permission.RECORD_AUDIO)
public suspend fun CameraState.toggleRecording(
    fileOutputOptions: FileOutputOptions,
    audioConfig: AudioConfig = AudioConfig.create(true),
): Uri? =
    suspendCancellableCoroutine { cont ->
        with(cont) { toggleRecording(fileOutputOptions, audioConfig, ::toggleRecordContinuation) }
    }

/**
 * Transform toggle recording content values options to suspend function
 * */
@RequiresApi(Build.VERSION_CODES.N)
@RequiresPermission(Manifest.permission.RECORD_AUDIO)
public suspend fun CameraState.toggleRecording(
    mediaStoreOutputOptions: MediaStoreOutputOptions,
    audioConfig: AudioConfig = AudioConfig.create(true),
): Uri? = suspendCancellableCoroutine { cont ->
    with(cont) { toggleRecording(mediaStoreOutputOptions, audioConfig, ::toggleRecordContinuation) }
}

/**
 * Transform toggle recording output files options to suspend function
 * */
@RequiresApi(Build.VERSION_CODES.O)
@RequiresPermission(Manifest.permission.RECORD_AUDIO)
public suspend fun CameraState.toggleRecording(
    fileDescriptorOutputOptions: FileDescriptorOutputOptions,
    audioConfig: AudioConfig = AudioConfig.create(true),
): Uri? = suspendCancellableCoroutine { cont ->
    with(cont) {
        toggleRecording(
            fileDescriptorOutputOptions,
            audioConfig,
            ::toggleRecordContinuation
        )
    }
}

private fun Continuation<Uri?>.takePictureContinuation(result: ImageCaptureResult) {
    when (val res: ImageCaptureResult = result) {
        is ImageCaptureResult.Error -> resumeWithException(res.throwable)
        is ImageCaptureResult.Success -> resume(res.savedUri)
    }
}

private fun Continuation<Uri?>.toggleRecordContinuation(result: VideoCaptureResult) {
    when (val res: VideoCaptureResult = result) {
        is VideoCaptureResult.Error -> resumeWithException(res.throwable ?: Exception(res.message))
        is VideoCaptureResult.Success -> resume(res.savedUri)
    }
}
