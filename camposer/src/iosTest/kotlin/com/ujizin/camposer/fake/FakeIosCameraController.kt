package com.ujizin.camposer.fake

import com.ujizin.camposer.internal.core.ios.IOSCameraController
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDevicePositionUnspecified
import platform.AVFoundation.AVCaptureDeviceType
import platform.AVFoundation.AVCaptureFlashMode
import platform.AVFoundation.AVCaptureFlashModeOff
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhotoQualityPrioritization
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationBalanced
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoStabilizationMode
import platform.AVFoundation.AVCaptureVideoStabilizationModeAuto
import platform.AVFoundation.AVLayerVideoGravity
import platform.CoreGraphics.CGPoint
import platform.UIKit.UIColor
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
class FakeIosCameraController : IOSCameraController {
  val fakeOutputs = mutableListOf<AVCaptureOutput>()
  var fakeIsFocusSupported = false
    private set
  var fakeFrameRate = 30
    private set
  var fakeVideoStabilizationMode = AVCaptureVideoStabilizationModeAuto
    private set
  var fakeDeviceFormat: AVCaptureDeviceFormat? = null
    private set
  var fakePreviewView: UIView? = null
    private set
  var fakeGravity: AVLayerVideoGravity = null
    private set
  var fakeCaptureDevice: AVCaptureDevice? = null
    private set
  var fakeAudioEnabled = false
    private set
  var fakeFocusPoint: CValue<CGPoint>? = null
    private set
  var fakeFlashMode: AVCaptureFlashMode = AVCaptureFlashModeOff
    private set
  var fakeZoomRatio: Float = 1.0f
    private set
  var fakeExposureCompensation: Float = 0f
    private set
  var fakeTorchEnabled = false
    private set
  var isFakeReleased = false
    private set
  var isFakeHighResolutionEnabled = false
    private set
  var fakeQuality: AVCapturePhotoQualityPrioritization = AVCapturePhotoQualityPrioritizationBalanced
    private set
  var fakeErrorInRecording: Boolean = false
    internal set
  var fakeIsFlashSupported: Boolean = true
    internal set
  var fakeIsExposureSupported: Boolean = true
    internal set
  var fakeIsZSLSupported: Boolean = true
    internal set

  private val mutableFakeIsMuted = MutableStateFlow(false)
  private val mutableFakeIsRecording = MutableStateFlow(false)

  private var position: AVCaptureDevicePosition = AVCaptureDevicePositionUnspecified

  override val captureSession: AVCaptureSession
    get() = AVCaptureSession()

  override val captureDeviceInput: AVCaptureDeviceInput
    get() = TODO("Not yet implemented")

  override val captureDevice: AVCaptureDevice
    get() = fakeCaptureDevice!!

  override val zoomRange: Pair<Float, Float>
    get() = 0F to 30F
  override val fpsRange: Pair<Int, Int>
    get() = 0 to 30
  override val exposureCompensationRange: Pair<Float, Float>
    get() = 0F to 8F
  override val isFlashAvailable: Boolean
    get() = fakeIsFlashSupported
  override val hasFlash: Boolean
    get() = fakeIsFlashSupported
  override val isTorchAvailable: Boolean
    get() = true
  override val hasTorch: Boolean
    get() = true

  override val isExposureCompensationSupported: Boolean
    get() = fakeIsExposureSupported

  override val isFocusSupported: Boolean
    get() = fakeIsFocusSupported

  override val isMuted: StateFlow<Boolean> = mutableFakeIsMuted.asStateFlow()

  override val isRecording: StateFlow<Boolean> = mutableFakeIsRecording.asStateFlow()

  override fun isZeroShutterLagSupported(output: AVCaptureOutput): Boolean = true

  override fun start(
    captureOutput: AVCaptureOutput,
    device: AVCaptureDevice,
    isMuted: Boolean,
    onRunningChanged: (Boolean) -> Unit,
  ) {
    fakeOutputs.add(captureOutput)
    fakeCaptureDevice = device
    setAudioEnabled(!isMuted)
    onRunningChanged(true)
  }

  override fun getCurrentPosition(): AVCaptureDevicePosition = position

  override fun getCurrentDeviceOrientation(): AVCaptureVideoOrientation =
    AVCaptureVideoOrientationPortrait

  override fun withSessionConfiguration(block: () -> Unit) {
    block()
  }

  override fun addOutput(output: AVCaptureOutput) {
    fakeOutputs.add(output)
  }

  override fun removeOutput(output: AVCaptureOutput) {
    fakeOutputs.remove(output)
  }

  override fun setFrameRate(frameRate: Int) {
    fakeFrameRate = frameRate
  }

  override fun setVideoStabilization(mode: AVCaptureVideoStabilizationMode) {
    fakeVideoStabilizationMode = mode
  }

  override fun isVideoStabilizationSupported(mode: AVCaptureVideoStabilizationMode): Boolean = true

  override fun setDeviceFormat(format: AVCaptureDeviceFormat) {
    fakeDeviceFormat = format
  }

  override fun renderPreviewLayer(view: UIView) {
    fakePreviewView = view
  }

  override fun setPreviewBackgroundColor(uiColor: UIColor) {
  }

  override fun setPreviewGravity(gravity: AVLayerVideoGravity) {
    fakeGravity = gravity
  }

  override fun setCaptureDevice(device: AVCaptureDevice) {
    fakeCaptureDevice = device
  }

  override fun setAudioEnabled(isEnabled: Boolean) {
    fakeAudioEnabled = isEnabled
  }

  override fun setFocusPoint(focusPoint: CValue<CGPoint>) {
    fakeFocusPoint = focusPoint
  }

  override fun setFlashMode(mode: AVCaptureFlashMode) {
    fakeFlashMode = mode
  }

  override fun setZoomRatio(zoomRatio: Float) {
    fakeZoomRatio = zoomRatio
  }

  override fun setExposureCompensation(exposureCompensation: Float) {
    fakeExposureCompensation = exposureCompensation
  }

  override fun setTorchEnabled(isTorchEnabled: Boolean) {
    fakeTorchEnabled = isTorchEnabled
  }

  override fun setCameraOutputQuality(
    quality: AVCapturePhotoQualityPrioritization,
    highResolutionEnabled: Boolean,
  ) {
    fakeQuality = quality
    isFakeHighResolutionEnabled = highResolutionEnabled
  }

  override fun getCaptureDevice(
    deviceTypes: List<AVCaptureDeviceType>,
    position: AVCaptureDevicePosition,
    uniqueId: String?,
  ): AVCaptureDevice {
    this.position = position

    return object : AVCaptureDevice() {
    }
  }

  override fun takePicture(
    isMirrorEnabled: Boolean,
    flashMode: AVCaptureFlashMode,
    videoOrientation: AVCaptureVideoOrientation,
    onPictureCaptured: (Result<ByteArray>) -> Unit,
  ) = onPictureCaptured(Result.success(byteArrayOf()))

  override fun takePicture(
    filename: String,
    isMirrorEnabled: Boolean,
    flashMode: AVCaptureFlashMode,
    videoOrientation: AVCaptureVideoOrientation,
    onPictureCaptured: (Result<String>) -> Unit,
  ) = onPictureCaptured(Result.success(filename))

  override fun startRecording(
    isMirrorEnabled: Boolean,
    videoOrientation: AVCaptureVideoOrientation,
    filename: String,
    onVideoCaptured: (Result<String>) -> Unit,
  ) {
    mutableFakeIsRecording.update { true }

    if (fakeErrorInRecording) {
      onVideoCaptured(Result.failure(Exception("Fake error")))
      return
    }

    onVideoCaptured(Result.success(filename))
  }

  override fun resumeRecording(): Result<Boolean> = Result.success(true)

  override fun pauseRecording(): Result<Boolean> = Result.success(true)

  override fun stopRecording(): Result<Boolean> {
    mutableFakeIsRecording.update { false }
    mutableFakeIsMuted.update { false }
    return Result.success(true)
  }

  override fun muteRecording(isMuted: Boolean): Result<Boolean> {
    mutableFakeIsMuted.update { isMuted }
    return Result.success(true)
  }

  override fun release() {
    isFakeReleased = true
  }
}
