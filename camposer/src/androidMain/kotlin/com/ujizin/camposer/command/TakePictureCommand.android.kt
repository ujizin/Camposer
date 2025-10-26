package com.ujizin.camposer.command

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCapture.OutputFileResults
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import com.ujizin.camposer.result.CaptureResult
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executor

internal actual class DefaultTakePictureCommand(
    private val controller: CameraController,
    private val mainExecutor: Executor,
    private val contentResolver: ContentResolver,
) : AndroidTakePictureCommand {

    actual override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit) {
        val byteArrayOS = ByteArrayOutputStream()
        takePicture(
            outputFileOptions = OutputFileOptions.Builder(byteArrayOS).build(),
            onResult = { result ->
                val result = when (result) {
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
    ): Unit = takePicture(OutputFileOptions.Builder(File(filename)).build()) { androidResult ->
        val result = when (androidResult) {
            is CaptureResult.Error -> CaptureResult.Error(androidResult.throwable)
            is CaptureResult.Success -> CaptureResult.Success(
                data = androidResult.data?.toString() ?: filename
            )
        }
        onImageCaptured(result)
    }

    override fun takePicture(
        contentValues: ContentValues,
        saveCollection: Uri,
        onResult: (CaptureResult<Uri?>) -> Unit,
    ) {
        takePicture(
            outputFileOptions = OutputFileOptions.Builder(
                /* contentResolver = */ contentResolver,
                /* saveCollection = */ saveCollection,
                /* contentValues = */ contentValues
            ).build(),
            onResult = onResult,
        )
    }

    override fun takePicture(file: File, onResult: (CaptureResult<Uri?>) -> Unit) {
        takePicture(
            outputFileOptions = OutputFileOptions.Builder(file).build(),
            onResult = onResult
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
                })
        } catch (exception: Exception) {
            onResult(CaptureResult.Error(exception))
        }
    }
}