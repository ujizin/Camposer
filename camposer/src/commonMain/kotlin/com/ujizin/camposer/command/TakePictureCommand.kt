package com.ujizin.camposer.command

import com.ujizin.camposer.result.CaptureResult
import kotlinx.io.files.Path

internal interface TakePictureCommand {
    fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit)
    fun takePicture(path: Path, onImageCaptured: (CaptureResult<Path>) -> Unit)
}

internal expect class DefaultTakePictureCommand : TakePictureCommand {
    override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit)
    override fun takePicture(
        path: Path,
        onImageCaptured: (CaptureResult<Path>) -> Unit
    )
}