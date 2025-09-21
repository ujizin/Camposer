package com.ujizin.camposer.result

import com.ujizin.camposer.state.CameraState

/**
 * Capture Result of taking picture or recording video.
 *
 * @see CameraState.takePicture
 * @see CameraState.stopRecording
 * */
public sealed interface CaptureResult<out T> {
    public data class Success<T>(val data: T) : CaptureResult<T>

    public data class Error(val throwable: Throwable) : CaptureResult<Nothing>
}
