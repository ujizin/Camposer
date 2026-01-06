package com.ujizin.camposer.internal.core.ios

import androidx.annotation.RestrictTo
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDeviceType
import platform.AVFoundation.AVCaptureFlashMode
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhotoQualityPrioritization
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVCaptureVideoStabilizationMode
import platform.AVFoundation.AVLayerVideoGravity
import platform.CoreGraphics.CGPoint
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface IOSCameraController {
  // iOS API
  public val captureSession: AVCaptureSession
  public val captureDeviceInput: AVCaptureDeviceInput
  public val captureDevice: AVCaptureDevice

  // Props
  public val isFocusSupported: Boolean
  public val zoomRange: Pair<Float, Float>
  public val fpsRange: Pair<Int, Int>
  public val exposureCompensationRange: Pair<Float, Float>

  public val isExposureCompensationSupported: Boolean
  public val isFlashAvailable: Boolean
  public val hasFlash: Boolean
  public val isTorchAvailable: Boolean
  public val hasTorch: Boolean

  public val isMuted: Boolean

  public val isRecording: Boolean

  public fun isZeroShutterLagSupported(output: AVCaptureOutput): Boolean

  public fun start(
    captureOutput: AVCaptureOutput,
    device: AVCaptureDevice,
    isMuted: Boolean,
    onRunningChanged: (Boolean) -> Unit,
  )

  public fun getCurrentPosition(): AVCaptureDevicePosition

  public fun getCurrentDeviceOrientation(): AVCaptureVideoOrientation

  public fun addOutput(output: AVCaptureOutput)

  public fun removeOutput(output: AVCaptureOutput)

  public fun setFrameRate(frameRate: Int)

  public fun setVideoStabilization(mode: AVCaptureVideoStabilizationMode)

  public fun isVideoStabilizationSupported(mode: AVCaptureVideoStabilizationMode): Boolean

  public fun setDeviceFormat(format: AVCaptureDeviceFormat)

  public fun renderPreviewLayer(view: UIView)

  public fun setPreviewGravity(gravity: AVLayerVideoGravity)

  public fun setCaptureDevice(device: AVCaptureDevice)

  public fun setAudioEnabled(isEnabled: Boolean)

  public fun setFocusPoint(focusPoint: CValue<CGPoint>)

  public fun setFlashMode(mode: AVCaptureFlashMode)

  public fun setZoomRatio(zoomRatio: Float)

  public fun setExposureCompensation(exposureCompensation: Float)

  public fun setTorchEnabled(isTorchEnabled: Boolean)

  public fun setCameraOutputQuality(
    quality: AVCapturePhotoQualityPrioritization,
    highResolutionEnabled: Boolean,
  )

  public fun getCaptureDevice(
    deviceTypes: List<AVCaptureDeviceType>,
    position: AVCaptureDevicePosition,
    uniqueId: String? = null,
  ): AVCaptureDevice?

  public fun takePicture(
    isMirrorEnabled: Boolean,
    flashMode: AVCaptureFlashMode,
    videoOrientation: AVCaptureVideoOrientation,
    onPictureCaptured: (Result<ByteArray>) -> Unit,
  )

  public fun takePicture(
    filename: String,
    isMirrorEnabled: Boolean,
    flashMode: AVCaptureFlashMode,
    videoOrientation: AVCaptureVideoOrientation,
    onPictureCaptured: (Result<String>) -> Unit,
  )

  public fun startRecording(
    isMirrorEnabled: Boolean,
    videoOrientation: AVCaptureVideoOrientation,
    filename: String,
    onVideoCaptured: (Result<String>) -> Unit,
  )

  public fun resumeRecording(): Result<Boolean>

  public fun pauseRecording(): Result<Boolean>

  public fun stopRecording(): Result<Boolean>

  public fun muteRecording(isMuted: Boolean): Result<Boolean>

  public fun release()
}
