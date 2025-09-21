package com.ujizin.camposer.controller.command

import com.ujizin.camposer.error.CameraNotRunningException
import com.ujizin.camposer.error.ErrorRecordVideoException
import com.ujizin.camposer.error.VideoOutputNotFoundException
import com.ujizin.camposer.extensions.firstIsInstanceOrNull
import com.ujizin.camposer.extensions.setMirrorEnabled
import kotlinx.io.files.Path
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureSession
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.darwin.NSObject

internal class RecordVideoController(
    private val captureSession: AVCaptureSession
) {

    private val videoRecordOutput: AVCaptureMovieFileOutput?
        get() = captureSession.outputs.firstIsInstanceOrNull<AVCaptureMovieFileOutput>()

    // This is needed because delegate is weak reference
    private var videoDelegate: AVCaptureFileOutputRecordingDelegateProtocol? = null

    fun start(
        path: Path,
        isMirrorEnabled: Boolean,
        onVideoCapture: (Result<Path>) -> Unit,
    ) {
        if (!captureSession.isRunning()) return onVideoCapture(
            Result.failure(CameraNotRunningException())
        )

        val videoRecordOutput = videoRecordOutput
        if (videoRecordOutput == null) {
            onVideoCapture(Result.failure(VideoOutputNotFoundException()))
            return
        }

        videoRecordOutput.setMirrorEnabled(isMirrorEnabled)

        val videoDelegate = object : NSObject(), AVCaptureFileOutputRecordingDelegateProtocol {
            override fun captureOutput(
                output: AVCaptureFileOutput,
                didFinishRecordingToOutputFileAtURL: NSURL,
                fromConnections: List<*>,
                error: NSError?
            ) {
                val result = when {
                    error != null -> Result.failure(
                        ErrorRecordVideoException(error)
                    )

                    else -> Result.success(path)
                }
                onVideoCapture(result)
                videoDelegate = null
            }
        }.apply { videoDelegate = this }

        videoRecordOutput.startRecordingToOutputFileURL(
            outputFileURL = NSURL.fileURLWithPath(path.toString()),
            recordingDelegate = videoDelegate,
        )
    }

    fun resume() {
        videoRecordOutput?.resumeRecording() ?: throw VideoOutputNotFoundException()
    }

    fun pause() {
        videoRecordOutput?.pauseRecording() ?: throw VideoOutputNotFoundException()
    }

    fun stop() {
        videoRecordOutput?.stopRecording() ?: throw VideoOutputNotFoundException()
    }
}