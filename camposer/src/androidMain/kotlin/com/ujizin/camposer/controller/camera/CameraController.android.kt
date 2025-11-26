package com.ujizin.camposer.controller.camera

import AndroidCameraController
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
import com.ujizin.camposer.CaptureResult
import java.io.File

public actual class CameraController : AndroidCameraController() {

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun startRecording(
        fileOutputOptions: FileOutputOptions,
        audioConfig: AudioConfig,
        onResult: (CaptureResult<Uri?>) -> Unit,
    ): Unit = recordController.runBind {
        startRecording(
            fileOutputOptions = fileOutputOptions,
            audioConfig = audioConfig,
            onResult = onResult
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun startRecording(
        fileDescriptorOutputOptions: FileDescriptorOutputOptions,
        audioConfig: AudioConfig,
        onResult: (CaptureResult<Uri?>) -> Unit,
    ): Unit = recordController.runBind {
        startRecording(
            fileDescriptorOutputOptions = fileDescriptorOutputOptions,
            audioConfig = audioConfig,
            onResult = onResult
        )
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun startRecording(
        mediaStoreOutputOptions: MediaStoreOutputOptions,
        audioConfig: AudioConfig,
        onResult: (CaptureResult<Uri?>) -> Unit,
    ): Unit = recordController.runBind {
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
    ): Unit = takePictureCommand.runBind {
        takePicture(
            contentValues = contentValues,
            saveCollection = saveCollection,
            onResult = onResult,
        )
    }

    override fun takePicture(
        file: File,
        onResult: (CaptureResult<Uri?>) -> Unit,
    ): Unit = takePictureCommand.runBind {
        takePicture(
            file = file,
            onResult = onResult,
        )
    }

    override fun takePicture(
        outputFileOptions: ImageCapture.OutputFileOptions,
        onResult: (CaptureResult<Uri?>) -> Unit,
    ): Unit = takePictureCommand.runBind {
        takePicture(
            outputFileOptions = outputFileOptions,
            onResult = onResult,
        )
    }
}
