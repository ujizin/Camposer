package com.ujizin.camposer.state

import android.net.Uri
import androidx.compose.runtime.Immutable

/**
 * Video Result of recording video.
 *
 * @see CameraState.startRecording
 * @see CameraState.toggleRecording
 * */
sealed interface VideoCaptureResult {
    @Immutable
    data class Success(val savedUri: Uri?) : VideoCaptureResult

    @Immutable
    data class Error(
        val videoCaptureError: Int,
        val message: String,
        val throwable: Throwable?
    ) : VideoCaptureResult
}
