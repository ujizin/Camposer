package com.ujizin.camposer.internal.command

import com.ujizin.camposer.internal.error.CameraNotRunningException
import com.ujizin.camposer.internal.error.ErrorTakePhotoException
import com.ujizin.camposer.internal.error.ErrorWritePhotoPathException
import com.ujizin.camposer.internal.error.NSDataNotFoundException
import com.ujizin.camposer.internal.error.PhotoOutputNotFoundException
import com.ujizin.camposer.internal.extensions.firstIsInstanceOrNull
import com.ujizin.camposer.internal.extensions.setMirrorEnabled
import com.ujizin.camposer.internal.extensions.toByteArray
import platform.AVFoundation.AVCaptureFlashMode
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVVideoCodecJPEG
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.fileDataRepresentation
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.writeToURL
import platform.darwin.NSObject

internal class IOSTakePictureCommand(
    private val captureSession: AVCaptureSession,
) {

    private var takePictureDelegate: AVCapturePhotoCaptureDelegateProtocol? = null

    operator fun invoke(
        isMirrorEnabled: Boolean,
        flashMode: AVCaptureFlashMode,
        videoOrientation: AVCaptureVideoOrientation,
        onPictureCaptured: (Result<ByteArray>) -> Unit,
    ) = takePicture(
        isMirrorEnabled = isMirrorEnabled,
        flashMode = flashMode,
        videoOrientation = videoOrientation,
        onPictureCaptured = { result ->
            transformPictureCaptured(
                result = result,
                transform = { nsData -> Result.success(nsData.toByteArray()) },
                onPictureCaptured = onPictureCaptured,
            )
        }
    )

    operator fun invoke(
        filename: String,
        isMirrorEnabled: Boolean,
        flashMode: AVCaptureFlashMode,
        videoOrientation: AVCaptureVideoOrientation,
        onPictureCaptured: (Result<String>) -> Unit,
    ) = takePicture(
        isMirrorEnabled = isMirrorEnabled,
        flashMode = flashMode,
        videoOrientation = videoOrientation,
        onPictureCaptured = { result ->
            transformPictureCaptured(
                result = result,
                transform = { nsData ->
                    when {
                        filename.writeData(nsData) -> Result.success(filename)
                        else -> Result.failure(ErrorWritePhotoPathException(filename))
                    }
                },
                onPictureCaptured = onPictureCaptured,
            )
        }
    )

    private fun takePicture(
        isMirrorEnabled: Boolean,
        flashMode: AVCaptureFlashMode,
        videoOrientation: AVCaptureVideoOrientation,
        onPictureCaptured: (Result<NSData>) -> Unit,
    ) {
        if (!captureSession.isRunning()) return onPictureCaptured(
            Result.failure(CameraNotRunningException())
        )

        val cameraOutput = captureSession.outputs.firstIsInstanceOrNull<AVCapturePhotoOutput>()
        if (cameraOutput == null) {
            onPictureCaptured(Result.failure(PhotoOutputNotFoundException()))
            return
        }

        val videoMediaType = cameraOutput.connectionWithMediaType(AVMediaTypeVideo)
        videoMediaType?.videoOrientation = videoOrientation

        val delegate = object : NSObject(), AVCapturePhotoCaptureDelegateProtocol {
            override fun captureOutput(
                output: AVCapturePhotoOutput,
                didFinishProcessingPhoto: AVCapturePhoto,
                error: NSError?,
            ) {
                val result = when {
                    error != null -> Result.failure(ErrorTakePhotoException(error))
                    else -> when (val nsData = didFinishProcessingPhoto.fileDataRepresentation()) {
                        null -> Result.failure(NSDataNotFoundException())
                        else -> Result.success(nsData)
                    }
                }

                onPictureCaptured(result)
                takePictureDelegate = null
            }
        }.apply { this@IOSTakePictureCommand.takePictureDelegate = this }

        cameraOutput.setMirrorEnabled(isMirrorEnabled)

        val settings = AVCapturePhotoSettings.photoSettingsWithFormat(
            mapOf(AVVideoCodecKey to AVVideoCodecJPEG)
        )
        settings.setFlashMode(flashMode)

        cameraOutput.capturePhotoWithSettings(settings, delegate)
    }

    private fun <T> transformPictureCaptured(
        result: Result<NSData>,
        transform: (NSData) -> Result<T>,
        onPictureCaptured: (Result<T>) -> Unit,
    ) {
        if (result.isFailure) {
            val exception = result.exceptionOrNull()!!
            onPictureCaptured(Result.failure(exception))
            return
        }

        onPictureCaptured(transform(result.getOrThrow()))
    }

    private fun String.writeData(data: NSData): Boolean {
        val url = NSURL.fileURLWithPath(this)
        return data.writeToURL(url, atomically = true)
    }
}