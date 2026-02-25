package com.ujizin.camposer.session

import androidx.annotation.RestrictTo
import com.ujizin.camposer.internal.OrientationManager
import com.ujizin.camposer.internal.command.IOSTakePictureCommand
import com.ujizin.camposer.internal.controller.IOSRecordController
import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.internal.error.AudioInputNotFoundException
import com.ujizin.camposer.internal.extensions.firstIsInstanceOrNull
import com.ujizin.camposer.internal.extensions.isFlashModeSupported
import com.ujizin.camposer.internal.extensions.toDeviceInput
import com.ujizin.camposer.internal.extensions.tryAddInput
import com.ujizin.camposer.internal.extensions.tryAddOutput
import com.ujizin.camposer.internal.extensions.withConfigurationLock
import com.ujizin.camposer.internal.utils.DispatchQueue.cameraQueue
import com.ujizin.camposer.manager.PreviewManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDevice.Companion.defaultDeviceWithMediaType
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDeviceSubjectAreaDidChangeNotification
import platform.AVFoundation.AVCaptureDeviceType
import platform.AVFoundation.AVCaptureExposureModeAutoExpose
import platform.AVFoundation.AVCaptureExposureModeContinuousAutoExposure
import platform.AVFoundation.AVCaptureFlashMode
import platform.AVFoundation.AVCaptureFocusModeAutoFocus
import platform.AVFoundation.AVCaptureFocusModeContinuousAutoFocus
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoQualityPrioritization
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVCaptureVideoStabilizationMode
import platform.AVFoundation.AVFrameRateRange
import platform.AVFoundation.AVLayerVideoGravity
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.exposureMode
import platform.AVFoundation.exposurePointOfInterest
import platform.AVFoundation.flashMode
import platform.AVFoundation.focusMode
import platform.AVFoundation.focusPointOfInterest
import platform.AVFoundation.hasFlash
import platform.AVFoundation.hasTorch
import platform.AVFoundation.isExposurePointOfInterestSupported
import platform.AVFoundation.isFlashAvailable
import platform.AVFoundation.isFocusPointOfInterestSupported
import platform.AVFoundation.isTorchAvailable
import platform.AVFoundation.maxAvailableVideoZoomFactor
import platform.AVFoundation.maxExposureTargetBias
import platform.AVFoundation.minAvailableVideoZoomFactor
import platform.AVFoundation.minExposureTargetBias
import platform.AVFoundation.position
import platform.AVFoundation.setExposureTargetBias
import platform.AVFoundation.setSubjectAreaChangeMonitoringEnabled
import platform.AVFoundation.torchMode
import platform.AVFoundation.videoZoomFactor
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGPointMake
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSKeyValueChangeNewKey
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.Foundation.removeObserver
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.foundation.NSKeyValueObservingProtocol

@OptIn(ExperimentalForeignApi::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class DefaultIOSCameraController internal constructor(
  override val captureSession: AVCaptureSession,
  internal val previewManager: PreviewManager,
  private val takePictureCommand: IOSTakePictureCommand = IOSTakePictureCommand(captureSession),
) : IOSCameraController {
  private val recordController: IOSRecordController = IOSRecordController(this)

  private var _captureDeviceInput: AVCaptureDeviceInput? = null

  override val captureDeviceInput: AVCaptureDeviceInput
    get() = _captureDeviceInput!!

  override val captureDevice: AVCaptureDevice
    get() = captureDeviceInput.device

  public val isRunning: Boolean
    get() = captureSession.isRunning()

  internal val orientationListener: OrientationManager = OrientationManager()

  private var runningObserver: NSObject? = null

  override val isFocusSupported: Boolean
    get() = captureDevice.isFocusPointOfInterestSupported() ||
      captureDevice.isExposurePointOfInterestSupported()

  override val zoomRange: Pair<Float, Float>
    get() = with(captureDevice) {
      minAvailableVideoZoomFactor.toFloat() to maxAvailableVideoZoomFactor.toFloat()
    }

  override val fpsRange: Pair<Int, Int>
    get() = captureDevice.activeFormat.videoSupportedFrameRateRanges
      .filterIsInstance<AVFrameRateRange>()
      .run {
        val min = minOf { it.minFrameRate }.toInt()
        val max = maxOf { it.maxFrameRate }.toInt()
        min to max
      }

  override val exposureCompensationRange: Pair<Float, Float>
    get() = with(captureDevice) {
      minExposureTargetBias to maxExposureTargetBias
    }

  override val isExposureCompensationSupported: Boolean
    get() = exposureCompensationRange.first != exposureCompensationRange.second

  override val isFlashAvailable: Boolean
    get() = captureDevice.isFlashAvailable()

  override val hasFlash: Boolean
    get() = captureDevice.hasFlash

  override val isTorchAvailable: Boolean
    get() = captureDevice.isTorchAvailable()

  override val hasTorch: Boolean
    get() = captureDevice.hasTorch

  override val isMuted: StateFlow<Boolean>
    get() = recordController.isMuted

  override val isRecording: StateFlow<Boolean>
    get() = recordController.isRecording

  private val audioInput: AVCaptureDeviceInput
    get() = defaultDeviceWithMediaType(AVMediaTypeAudio)?.toDeviceInput()
      ?: throw AudioInputNotFoundException()
  internal val frameRateRanges: List<AVFrameRateRange>
    get() = captureDevice.activeFormat.videoSupportedFrameRateRanges
      .filterIsInstance<AVFrameRateRange>()

  internal val minFrameRate: Int
    get() = frameRateRanges.minOf { it.minFrameRate }.toInt()

  internal val maxFrameRate: Int
    get() = frameRateRanges.maxOf { it.maxFrameRate }.toInt()

  public val previewLayer: AVCaptureVideoPreviewLayer = previewManager.videoPreviewLayer

  private val completeFocusObserver = object : NSObject() {
    @OptIn(BetaInteropApi::class)
    @ObjCAction
    private fun onFocusCompleted(notification: NSNotification?) = onFocusCompleted()
  }

  override fun addOutput(output: AVCaptureOutput) {
    captureSession.tryAddOutput(output)
  }

  override fun removeOutput(output: AVCaptureOutput) {
    captureSession.removeOutput(output)
  }

  override fun withSessionConfiguration(block: () -> Unit) {
    captureSession.beginConfiguration()
    block()
    captureSession.commitConfiguration()
  }

  override fun start(
    captureOutput: AVCaptureOutput,
    device: AVCaptureDevice,
    isMuted: Boolean,
    onRunningChanged: (Boolean) -> Unit,
  ): Unit =
    dispatch_async(cameraQueue) {
      captureSession.beginConfiguration()

      setCaptureDevice(device)
      setAudioEnabled(!isMuted)
      captureSession.tryAddOutput(captureOutput)
      captureSession.commitConfiguration()

      previewManager.start(captureSession)

      orientationListener.start()
      captureSession.startRunning()

      runningObserver = object : NSObject(), NSKeyValueObservingProtocol {
        override fun observeValueForKeyPath(
          keyPath: String?,
          ofObject: Any?,
          change: Map<Any?, *>?,
          context: COpaquePointer?,
        ) {
          onRunningChanged(change?.get(NSKeyValueChangeNewKey) as? Boolean == true)
        }
      }
    }

  override fun getCurrentDeviceOrientation(): AVCaptureVideoOrientation =
    orientationListener.currentOrientation

  override fun setFrameRate(frameRate: Int) {
    val captureDevice = captureDevice
    if (frameRate !in minFrameRate..maxFrameRate) {
      return
    }

    val minFps = CMTimeMake(1, frameRate)
    val maxFps = CMTimeMake(1, frameRate)

    captureDevice.withConfigurationLock {
      captureDevice.activeVideoMinFrameDuration = minFps
      captureDevice.activeVideoMaxFrameDuration = maxFps
    }
  }

  override fun getCurrentPosition(): AVCaptureDevicePosition = captureDeviceInput.device.position

  override fun isVideoStabilizationSupported(mode: AVCaptureVideoStabilizationMode): Boolean =
    captureDevice.activeFormat.isVideoStabilizationModeSupported(mode)

  override fun setVideoStabilization(mode: AVCaptureVideoStabilizationMode) {
    val output = captureSession.outputs.firstIsInstanceOrNull<AVCaptureMovieFileOutput>()
    val videoConnection = output?.connectionWithMediaType(AVMediaTypeVideo)

    if (isVideoStabilizationSupported(mode)) {
      return
    }

    captureDevice.withConfigurationLock {
      videoConnection?.preferredVideoStabilizationMode = mode
    }
  }

  override fun setDeviceFormat(format: AVCaptureDeviceFormat) {
    if (!captureDevice.formats().contains(format)) {
      return
    }

    captureDevice.withConfigurationLock { captureDevice.setActiveFormat(format) }
  }

  override fun setPreviewBackgroundColor(uiColor: UIColor) {
    previewManager.setBackgroundColor(uiColor)
  }

  override fun setPreviewGravity(gravity: AVLayerVideoGravity) {
    previewManager.setGravity(gravity)
  }

  override fun renderPreviewLayer(view: UIView) {
    previewManager.attachView(view)
  }

  override fun setCaptureDevice(device: AVCaptureDevice) {
    if (_captureDeviceInput?.device == device) {
      return
    }

    captureSession.beginConfiguration()

    // Remove the previous one
    if (_captureDeviceInput != null) {
      captureSession.removeInput(captureDeviceInput)
    }

    _captureDeviceInput = device.toDeviceInput()
    captureSession.tryAddInput(captureDeviceInput)
    captureSession.commitConfiguration()
  }

  override fun setAudioEnabled(isEnabled: Boolean) {
    when {
      isEnabled -> captureSession.tryAddInput(audioInput)
      else -> captureSession.removeInput(audioInput)
    }
  }

  override fun setFocusPoint(focusPoint: CValue<CGPoint>): Unit =
    captureDevice.withConfigurationLock {
      if (isExposurePointOfInterestSupported()) {
        exposurePointOfInterest = focusPoint
        exposureMode = AVCaptureExposureModeAutoExpose
      }

      if (isFocusPointOfInterestSupported()) {
        focusPointOfInterest = focusPoint
        focusMode = AVCaptureFocusModeAutoFocus
      }

      val notificationCenter = NSNotificationCenter.defaultCenter
      notificationCenter.removeObserver(
        observer = completeFocusObserver,
        name = AVCaptureDeviceSubjectAreaDidChangeNotification,
        `object` = null,
      )

      captureDevice.setSubjectAreaChangeMonitoringEnabled(true)

      notificationCenter.addObserver(
        observer = completeFocusObserver,
        selector = NSSelectorFromString("${::onFocusCompleted.name}:"),
        name = AVCaptureDeviceSubjectAreaDidChangeNotification,
        `object` = null,
      )
    }

  @OptIn(BetaInteropApi::class)
  @ObjCAction
  private fun onFocusCompleted(): Unit =
    captureDevice.withConfigurationLock {
      val centerFocusPoint = CGPointMake(0.5, 0.5)
      if (isFocusPointOfInterestSupported()) {
        focusPointOfInterest = centerFocusPoint
        focusMode = AVCaptureFocusModeContinuousAutoFocus
      }

      if (isExposurePointOfInterestSupported()) {
        exposurePointOfInterest = centerFocusPoint
        exposureMode = AVCaptureExposureModeContinuousAutoExposure
      }

      captureDevice.setSubjectAreaChangeMonitoringEnabled(false)

      NSNotificationCenter.defaultCenter.removeObserver(
        observer = completeFocusObserver,
        name = AVCaptureDeviceSubjectAreaDidChangeNotification,
        `object` = null,
      )
    }

  override fun setTorchEnabled(isTorchEnabled: Boolean) {
    if (!captureDevice.hasTorch || !captureDevice.isTorchAvailable()) return

    captureDevice.withConfigurationLock {
      torchMode = if (isTorchEnabled) AVCaptureTorchModeOn else AVCaptureTorchModeOff
    }
  }

  override fun setFlashMode(mode: AVCaptureFlashMode) {
    val isFlashModeAvailable = captureDevice.hasFlash && captureDevice.isFlashAvailable()
    if (!isFlashModeAvailable || !captureSession.isFlashModeSupported(mode)) {
      return
    }

    captureDevice.withConfigurationLock { flashMode = mode }
  }

  override fun setZoomRatio(zoomRatio: Float) {
    captureDevice.withConfigurationLock { videoZoomFactor = zoomRatio.toDouble() }
  }

  override fun setExposureCompensation(exposureCompensation: Float) {
    captureDevice.withConfigurationLock {
      setExposureTargetBias(bias = exposureCompensation, completionHandler = {})
    }
  }

  override fun setCameraOutputQuality(
    quality: AVCapturePhotoQualityPrioritization,
    highResolutionEnabled: Boolean,
  ) {
    val output = captureSession.outputs.firstIsInstanceOrNull<AVCapturePhotoOutput>()
    output?.setMaxPhotoQualityPrioritization(quality)
    output?.setHighResolutionCaptureEnabled(highResolutionEnabled)
  }

  override fun isZeroShutterLagSupported(output: AVCaptureOutput): Boolean =
    (output as? AVCapturePhotoOutput)?.isZeroShutterLagSupported()
      ?: false

  override fun getCaptureDevice(
    deviceTypes: List<AVCaptureDeviceType>,
    position: AVCaptureDevicePosition,
    uniqueId: String?,
  ): AVCaptureDevice? =
    AVCaptureDeviceDiscoverySession
      .discoverySessionWithDeviceTypes(
        deviceTypes,
        AVMediaTypeVideo,
        position,
      ).devices
      .firstOrNull {
        val device = it as? AVCaptureDevice
        when {
          uniqueId != null -> uniqueId == device?.uniqueID
          else -> device?.position == position
        }
      } as? AVCaptureDevice

  override fun takePicture(
    isMirrorEnabled: Boolean,
    flashMode: AVCaptureFlashMode,
    videoOrientation: AVCaptureVideoOrientation,
    onPictureCaptured: (Result<ByteArray>) -> Unit,
  ): Unit =
    takePictureCommand(
      isMirrorEnabled = isMirrorEnabled,
      flashMode = flashMode,
      videoOrientation = videoOrientation,
      onPictureCaptured = onPictureCaptured,
    )

  override fun takePicture(
    filename: String,
    isMirrorEnabled: Boolean,
    flashMode: AVCaptureFlashMode,
    videoOrientation: AVCaptureVideoOrientation,
    onPictureCaptured: (Result<String>) -> Unit,
  ): Unit =
    takePictureCommand(
      filename = filename,
      isMirrorEnabled = isMirrorEnabled,
      flashMode = flashMode,
      videoOrientation = videoOrientation,
      onPictureCaptured = onPictureCaptured,
    )

  override fun startRecording(
    isMirrorEnabled: Boolean,
    videoOrientation: AVCaptureVideoOrientation,
    filename: String,
    onVideoCaptured: (Result<String>) -> Unit,
  ): Unit =
    recordController.start(
      filename = filename,
      isMirrorEnabled = isMirrorEnabled,
      videoOrientation = videoOrientation,
      onVideoCaptured = onVideoCaptured,
    )

  override fun resumeRecording(): Result<Boolean> = recordController.resume()

  override fun pauseRecording(): Result<Boolean> = recordController.pause()

  override fun stopRecording(): Result<Boolean> = recordController.stop()

  override fun muteRecording(isMuted: Boolean): Result<Boolean> = recordController.mute(isMuted)

  override fun release() {
    runningObserver?.let {
      captureSession.removeObserver(it, RUNNING_KEY_PATH)
    }
    runningObserver = null
    orientationListener.stop()
    _captureDeviceInput = null
    captureSession.stopRunning()
    previewManager.detachView()
  }

  internal companion object {
    private const val RUNNING_KEY_PATH = "running"
  }
}
