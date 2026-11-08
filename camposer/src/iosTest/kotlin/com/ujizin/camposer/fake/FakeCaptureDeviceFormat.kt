package com.ujizin.camposer.fake

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureVideoStabilizationMode
import platform.AVFoundation.AVFrameRateRange
import platform.CoreMedia.CMFormatDescriptionRef
import platform.CoreMedia.CMVideoDimensions
import platform.CoreMedia.CMVideoFormatDescriptionCreate
import platform.CoreMedia.CMVideoFormatDescriptionRefVar
import platform.CoreMedia.kCMVideoCodecType_H264

@OptIn(ExperimentalForeignApi::class)
internal class FakeCaptureDeviceFormat(
  private val width: Int,
  private val height: Int,
  private val maxFps: Double = 30.0,
) : AVCaptureDeviceFormat() {
  private val fakeFormatDescription: CMFormatDescriptionRef? = memScoped {
    val description = alloc<CMVideoFormatDescriptionRefVar>()
    CMVideoFormatDescriptionCreate(
      null,
      kCMVideoCodecType_H264,
      width,
      height,
      null,
      description.ptr,
    )
    description.value
  }

  private val fakeFrameRateRange = FakeFrameRateRange(maxFps)

  override fun formatDescription(): CMFormatDescriptionRef? = fakeFormatDescription

  override fun highResolutionStillImageDimensions(): CValue<CMVideoDimensions> =
    cValue {
      width = this@FakeCaptureDeviceFormat.width
      height = this@FakeCaptureDeviceFormat.height
    }

  override fun supportedMaxPhotoDimensions(): List<*> = emptyList<Any>()

  override fun videoSupportedFrameRateRanges(): List<*> = listOf(fakeFrameRateRange)

  override fun isVideoStabilizationModeSupported(
    videoStabilizationMode: AVCaptureVideoStabilizationMode,
  ): Boolean = false

  // The real description reads format internals this fake does not have and crashes.
  override fun description(): String = "FakeCaptureDeviceFormat(${width}x$height)"
}

@OptIn(ExperimentalForeignApi::class)
private class FakeFrameRateRange(
  private val maxFps: Double,
) : AVFrameRateRange() {
  override fun minFrameRate(): Double = 1.0

  override fun maxFrameRate(): Double = maxFps
}
