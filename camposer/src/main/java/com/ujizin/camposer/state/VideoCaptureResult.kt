package com.ujizin.camposer.state

import android.net.Uri
import androidx.compose.runtime.Immutable

/**
 * Video Result of recording video.
 *
 * @see CameraState.startRecording
 * @see CameraState.toggleRecording
 * */
public sealed interface VideoCaptureResult {
    @Immutable
    public data class Success(val savedUri: Uri?) : VideoCaptureResult

    @Immutable
    public data class Error(
        val videoCaptureError: Int,
        val message: String,
        val throwable: Throwable?
    ) : VideoCaptureResult
}
