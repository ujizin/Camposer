package com.ujizin.camposer.internal.controller

import com.ujizin.camposer.internal.core.ios.IOSCameraController
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDevicePositionUnspecified
import platform.AVFoundation.AVCaptureDeviceType
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhotoQualityPrioritization
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoStabilizationMode
import platform.AVFoundation.AVErrorRecordingSuccessfullyFinishedKey
import platform.AVFoundation.AVLayerVideoGravity
import platform.CoreGraphics.CGPoint
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.UIKit.UIColor
import platform.UIKit.UIView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class)
internal class IOSRecordControllerTest {
  @Test
  fun test_stop_does_not_duplicate_audio_cleanup_when_delegate_finishes() {
    val output = TestMovieFileOutput()
    val cameraController = TestIOSCameraController(output)
    val recordController = IOSRecordController(cameraController)
    var callbackCount = 0

    recordController.start(
      filename = "/video/video.mp4",
      videoOrientation = AVCaptureVideoOrientationPortrait,
      isMirrorEnabled = false,
      onVideoCaptured = { callbackCount++ },
    )

    assertEquals(listOf(true), cameraController.audioEnabledCalls)
    assertTrue(recordController.isRecording.value)

    recordController.stop()

    assertEquals(listOf(true, false), cameraController.audioEnabledCalls)
    assertFalse(recordController.isRecording.value)
    assertFalse(recordController.isMuted.value)

    output.finishRecording()

    assertEquals(listOf(true, false), cameraController.audioEnabledCalls)
    assertEquals(1, callbackCount)
    assertFalse(recordController.isRecording.value)
    assertFalse(recordController.isMuted.value)
  }

  @Test
  fun test_recording_stopped_error_is_ignored_when_avfoundation_marks_file_playable() {
    val output = TestMovieFileOutput()
    val cameraController = TestIOSCameraController(output)
    val recordController = IOSRecordController(cameraController)
    var capturedResult: Result<String>? = null

    recordController.start(
      filename = "/video/video.mp4",
      videoOrientation = AVCaptureVideoOrientationPortrait,
      isMirrorEnabled = false,
      onVideoCaptured = { capturedResult = it },
    )

    output.finishRecording(
      error = NSError.errorWithDomain(
        domain = "AVFoundationErrorDomain",
        code = -11818,
        userInfo = mapOf(AVErrorRecordingSuccessfullyFinishedKey to true),
      ),
    )

    val result = checkNotNull(capturedResult)
    assertTrue(result.isSuccess)
    assertEquals("/video/video.mp4", result.getOrNull())
    assertFalse(recordController.isRecording.value)
    assertFalse(recordController.isMuted.value)
  }
}

private class TestMovieFileOutput : AVCaptureMovieFileOutput() {
  private var delegate: AVCaptureFileOutputRecordingDelegateProtocol? = null
  private var recordedUrl: NSURL? = null

  override fun startRecordingToOutputFileURL(
    outputFileURL: NSURL,
    recordingDelegate: AVCaptureFileOutputRecordingDelegateProtocol,
  ) {
    recordedUrl = outputFileURL
    delegate = recordingDelegate
  }

  fun finishRecording(error: NSError? = null) {
    delegate?.captureOutput(
      output = this as AVCaptureFileOutput,
      didFinishRecordingToOutputFileAtURL = checkNotNull(recordedUrl),
      fromConnections = emptyList<Any>(),
      error = error,
    )
  }
}

@OptIn(ExperimentalForeignApi::class)
private class TestIOSCameraController(
  movieFileOutput: AVCaptureMovieFileOutput,
) : IOSCameraController {
  val audioEnabledCalls = mutableListOf<Boolean>()

  override val captureSession: AVCaptureSession = AVCaptureSession().apply {
    addOutput(movieFileOutput)
    startRunning()
  }

  override val captureDeviceInput: AVCaptureDeviceInput
    get() = error("Unused in test")

  override val captureDevice: AVCaptureDevice
    get() = error("Unused in test")

  override val isFocusSupported: Boolean = false
  override val zoomRange: Pair<Float, Float> = 1f to 1f
  override val fpsRange: Pair<Int, Int> = 30 to 30
  override val exposureCompensationRange: Pair<Float, Float> = 0f to 0f
  override val isExposureCompensationSupported: Boolean = false
  override val isFlashAvailable: Boolean = false
  override val hasFlash: Boolean = false
  override val isTorchAvailable: Boolean = false
  override val hasTorch: Boolean = false

  override val isMuted: StateFlow<Boolean> = MutableStateFlow(false)
  override val isRecording: StateFlow<Boolean> = MutableStateFlow(false)

  override fun isZeroShutterLagSupported(output: AVCaptureOutput): Boolean = false

  override fun start(
    captureOutput: AVCaptureOutput,
    device: AVCaptureDevice,
    onRunningChanged: (Boolean) -> Unit,
  ) = error("Unused in test")

  override fun getCurrentPosition(): AVCaptureDevicePosition = AVCaptureDevicePositionUnspecified

  override fun getCurrentDeviceOrientation(): AVCaptureVideoOrientation =
    AVCaptureVideoOrientationPortrait

  override fun withSessionConfiguration(block: () -> Unit) = block()

  override fun addOutput(output: AVCaptureOutput) = error("Unused in test")

  override fun removeOutput(output: AVCaptureOutput) = error("Unused in test")

  override fun setFrameRate(frameRate: Int) = error("Unused in test")

  override fun setVideoStabilization(mode: AVCaptureVideoStabilizationMode) =
    error("Unused in test")

  override fun isVideoStabilizationSupported(mode: AVCaptureVideoStabilizationMode): Boolean = false

  override fun setDeviceFormat(format: AVCaptureDeviceFormat) = error("Unused in test")

  override fun renderPreviewLayer(view: UIView) = error("Unused in test")

  override fun setPreviewGravity(gravity: AVLayerVideoGravity) = error("Unused in test")

  override fun setPreviewBackgroundColor(uiColor: UIColor) = error("Unused in test")

  override fun setCaptureDevice(device: AVCaptureDevice) = error("Unused in test")

  override fun setAudioEnabled(isEnabled: Boolean) {
    audioEnabledCalls += isEnabled
  }

  override fun setFocusPoint(focusPoint: CValue<CGPoint>) = error("Unused in test")

  override fun setFlashMode(mode: platform.AVFoundation.AVCaptureFlashMode) =
    error("Unused in test")

  override fun setZoomRatio(zoomRatio: Float) = error("Unused in test")

  override fun setExposureCompensation(exposureCompensation: Float) = error("Unused in test")

  override fun setTorchEnabled(isTorchEnabled: Boolean) = error("Unused in test")

  override fun setCameraOutputQuality(
    quality: AVCapturePhotoQualityPrioritization,
    highResolutionEnabled: Boolean,
  ) = error("Unused in test")

  override fun getCaptureDevice(
    deviceTypes: List<AVCaptureDeviceType>,
    position: AVCaptureDevicePosition,
    uniqueId: String?,
  ): AVCaptureDevice? = error("Unused in test")

  override fun takePicture(
    isMirrorEnabled: Boolean,
    flashMode: platform.AVFoundation.AVCaptureFlashMode,
    videoOrientation: AVCaptureVideoOrientation,
    onPictureCaptured: (Result<ByteArray>) -> Unit,
  ) = error("Unused in test")

  override fun takePicture(
    filename: String,
    isMirrorEnabled: Boolean,
    flashMode: platform.AVFoundation.AVCaptureFlashMode,
    videoOrientation: AVCaptureVideoOrientation,
    onPictureCaptured: (Result<String>) -> Unit,
  ) = error("Unused in test")

  override fun startRecording(
    isMirrorEnabled: Boolean,
    videoOrientation: AVCaptureVideoOrientation,
    filename: String,
    onVideoCaptured: (Result<String>) -> Unit,
  ) = error("Unused in test")

  override fun resumeRecording(): Result<Boolean> = error("Unused in test")

  override fun pauseRecording(): Result<Boolean> = error("Unused in test")

  override fun stopRecording(): Result<Boolean> = error("Unused in test")

  override fun muteRecording(isMuted: Boolean): Result<Boolean> = error("Unused in test")

  override fun release() = Unit
}
