package com.ujizin.camposer.state

import android.net.Uri
import androidx.compose.runtime.Immutable

/**
 * Photo Result of taking picture.
 *
 * @see CameraState.takePicture
 * */
sealed interface ImageCaptureResult {
    @Immutable
    data class Success(val savedUri: Uri?) : ImageCaptureResult

    @Immutable
    data class Error(val throwable: Throwable) : ImageCaptureResult
}
