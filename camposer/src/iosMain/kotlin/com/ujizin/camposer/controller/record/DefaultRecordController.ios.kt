package com.ujizin.camposer.controller.record

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.error.CameraNotRunningException
import com.ujizin.camposer.error.ErrorRecordVideoException
import com.ujizin.camposer.error.VideoOutputNotFoundException
import com.ujizin.camposer.extensions.firstIsInstanceOrNull
import com.ujizin.camposer.extensions.setMirrorEnabled
import com.ujizin.camposer.extensions.toCaptureResult
import com.ujizin.camposer.result.CaptureResult
import com.ujizin.camposer.session.IOSCameraSession
import com.ujizin.camposer.config.properties.CaptureMode
import kotlinx.io.files.Path
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.position
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.darwin.NSObject

internal actual class DefaultRecordController(
    private val iosCameraSession: IOSCameraSession,
    private val captureModeProvider: RecordCaptureModeProvider,
) : RecordController {

    private val captureSession: AVCaptureSession
        get() = iosCameraSession.captureSession

    private val videoRecordOutput: AVCaptureMovieFileOutput?
        get() = captureSession.outputs.firstIsInstanceOrNull<AVCaptureMovieFileOutput>()

    private var videoDelegate: AVCaptureFileOutputRecordingDelegateProtocol? = null

    actual override var isMuted: Boolean by mutableStateOf(false)
    actual override var isRecording: Boolean by mutableStateOf(false)

    actual override fun startRecording(
        path: Path,
        onVideoCaptured: (CaptureResult<Path>) -> Unit
    ) = start(
        isMirrorEnabled = iosCameraSession.captureDeviceInput.device.position == AVCaptureDevicePositionFront,
        path = path,
        onVideoCapture = { result -> onVideoCaptured(result.toCaptureResult()) },
    ).apply { isRecording = true }

    actual override fun resumeRecording() {
        require(captureModeProvider.get() == CaptureMode.Video) { "Capture mode must be CaptureMode.Video" }

        videoRecordOutput?.resumeRecording() ?: throw VideoOutputNotFoundException()
    }

    actual override fun pauseRecording() {
        require(captureModeProvider.get() == CaptureMode.Video) { "Capture mode must be CaptureMode.Video" }

        videoRecordOutput?.pauseRecording() ?: throw VideoOutputNotFoundException()
    }

    actual override fun stopRecording() {
        require(captureModeProvider.get() == CaptureMode.Video) { "Capture mode must be CaptureMode.Video" }
        videoRecordOutput?.stopRecording() ?: throw VideoOutputNotFoundException()
        isRecording = false
        isMuted = false
    }

    actual override fun muteRecording(isMuted: Boolean) {
        this.isMuted = isMuted
        iosCameraSession.setAudioEnabled(!isMuted)
    }

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
                    error != null -> Result.failure(ErrorRecordVideoException(error))
                    else -> Result.success(path)
                }
                onVideoCapture(result)
                videoDelegate = null
            }
        }.apply { videoDelegate = this }

        videoRecordOutput.startRecordingToOutputFileURL(
            outputFileURL = NSURL.Companion.fileURLWithPath(path.toString()),
            recordingDelegate = videoDelegate,
        )
    }
}