package com.ujizin.camposer.state

import android.net.Uri
import androidx.compose.runtime.Immutable

/**
 * Photo Result of taking picture.
 *
 * @see CameraState.takePicture
 * */
public sealed interface ImageCaptureResult {
    @Immutable
    public data class Success(val savedUri: Uri?) : ImageCaptureResult

    @Immutable
    public data class Error(val throwable: Throwable) : ImageCaptureResult
}
