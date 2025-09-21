package com.ujizin.camposer.controller.command

import com.ujizin.camposer.error.CameraNotRunningException
import com.ujizin.camposer.error.ErrorTakePhotoException
import com.ujizin.camposer.error.ErrorWritePhotoPathException
import com.ujizin.camposer.error.NSDataNotFoundException
import com.ujizin.camposer.error.PhotoOutputNotFoundException
import com.ujizin.camposer.extensions.firstIsInstanceOrNull
import com.ujizin.camposer.extensions.setMirrorEnabled
import com.ujizin.camposer.extensions.toByteArray
import com.ujizin.camposer.extensions.writeData
import kotlinx.io.files.Path
import platform.AVFoundation.AVCaptureFlashMode
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVVideoCodecJPEG
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.fileDataRepresentation
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.darwin.NSObject

internal class TakePictureCommand(
    private val captureSession: AVCaptureSession,
) {

    operator fun invoke(
        isMirrorEnabled: Boolean,
        flashMode: AVCaptureFlashMode,
        onPictureCaptured: (Result<ByteArray>) -> Unit,
    ) = takePicture(
        isMirrorEnabled = isMirrorEnabled,
        flashMode = flashMode,
        onPictureCaptured = { result ->
            transformPictureCaptured(
                result = result,
                transform = { nsData -> Result.success(nsData.toByteArray()) },
                onPictureCaptured = onPictureCaptured,
            )
        }
    )

    operator fun invoke(
        path: Path,
        isMirrorEnabled: Boolean,
        flashMode: AVCaptureFlashMode,
        onPictureCaptured: (Result<Path>) -> Unit,
    ) = takePicture(
        isMirrorEnabled = isMirrorEnabled,
        flashMode = flashMode,
        onPictureCaptured = { result ->
            transformPictureCaptured(
                result = result,
                transform = { nsData ->
                    when {
                        path.writeData(nsData) -> Result.success(path)
                        else -> Result.failure(ErrorWritePhotoPathException(path))
                    }
                },
                onPictureCaptured = onPictureCaptured,
            )
        }
    )

    private fun takePicture(
        isMirrorEnabled: Boolean,
        flashMode: AVCaptureFlashMode,
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

        val delegate = object : NSObject(), AVCapturePhotoCaptureDelegateProtocol {
            override fun captureOutput(
                output: AVCapturePhotoOutput,
                didFinishProcessingPhoto: AVCapturePhoto,
                error: NSError?
            ) {
                val result = when {
                    error != null -> Result.failure(ErrorTakePhotoException(error))
                    else -> when (val nsData = didFinishProcessingPhoto.fileDataRepresentation()) {
                        null -> Result.failure(NSDataNotFoundException())
                        else -> Result.success(nsData)
                    }
                }

                onPictureCaptured(result)
            }
        }



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
}
