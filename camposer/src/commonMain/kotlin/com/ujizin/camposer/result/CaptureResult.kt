package com.ujizin.camposer.result

/**
 * Capture Result of taking picture or recording video.
 *
 * @see com.ujizin.camposer.controller.camera.CameraController.takePicture
 * @see com.ujizin.camposer.controller.camera.CameraController.startRecording
 * */
public sealed interface CaptureResult<out T> {
    public data class Success<T>(val data: T) : CaptureResult<T>

    public data class Error(val throwable: Throwable) : CaptureResult<Nothing>
}
