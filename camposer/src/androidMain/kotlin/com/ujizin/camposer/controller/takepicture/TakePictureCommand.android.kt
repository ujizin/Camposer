package com.ujizin.camposer.controller.takepicture

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCapture.OutputFileResults
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import com.ujizin.camposer.CaptureResult
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executor

public interface AndroidTakePictureCommand : TakePictureCommand {

    /**
     *  Take a picture with the camera.
     *
     *  @param saveCollection Uri collection where the photo will be saved.
     *  @param contentValues Content values of the photo.
     *  @param onResult Callback called when [CaptureResult<Uri?>] is ready
     * */
    public fun takePicture(
        contentValues: ContentValues,
        saveCollection: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        onResult: (CaptureResult<Uri?>) -> Unit,
    )

    /**
     * Take a picture with the camera.
     * @param file file where the photo will be saved
     * @param onResult Callback called when [CaptureResult<Uri?>] is ready
     * */
    public fun takePicture(
        file: File, onResult: (CaptureResult<Uri?>) -> Unit,
    )

    /**
     * Take a picture with the camera.
     *
     * @param outputFileOptions Output file options of the photo.
     * @param onResult Callback called when [CaptureResult] is ready
     * */
    public fun takePicture(
        outputFileOptions: OutputFileOptions,
        onResult: (CaptureResult<Uri?>) -> Unit,
    )
}

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