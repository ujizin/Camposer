package com.ujizin.camposer.internal.utils

import com.ujizin.camposer.internal.extensions.systemVersion
import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.CameraData.Companion.DEVICE_FORMAT
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.value
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.placeTo
import kotlinx.cinterop.pointed
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVFrameRateRange
import platform.CoreMedia.CMVideoDimensions
import platform.CoreMedia.CMVideoFormatDescriptionGetDimensions
import platform.UIKit.UIDevice

@OptIn(ExperimentalForeignApi::class)
internal object CameraFormatUtils {
  fun getPhotoFormats(formats: List<AVCaptureDeviceFormat>): List<CameraData> =
    formats.map { format ->
      memScoped {
        val fallback = format.highResolutionStillImageDimensions.placeTo(this).pointed

        val dimen = if (UIDevice.systemVersion >= 16.0) {
          format.supportedMaxPhotoDimensions
            .filterIsInstance<CMVideoDimensions>()
            .maxByOrNull { it.width * it.height } ?: fallback
        } else {
          fallback
        }

        CameraData(
          width = dimen.width,
          height = dimen.height,
          isFocusSupported = true,
          minFps = null,
          maxFps = null,
          videoStabilizationModes = null,
          metadata = hashMapOf(DEVICE_FORMAT to format),
        )
      }
    }

  fun getVideoFormats(formats: List<AVCaptureDeviceFormat>): List<CameraData> =
    formats.map { format ->
      memScoped {
        val dimen = CMVideoFormatDescriptionGetDimensions(
          format.formatDescription,
        ).placeTo(this).pointed

        val videoStabilizationModes = VideoStabilizationMode.entries.filter {
          format.isVideoStabilizationModeSupported(it.value)
        }

        val frameRates = format.videoSupportedFrameRateRanges
          .filterIsInstance<AVFrameRateRange>()

        CameraData(
          width = dimen.width,
          height = dimen.height,
          isFocusSupported = true,
          minFps = frameRates.minOf { it.minFrameRate }.toInt(),
          maxFps = frameRates.maxOf { it.maxFrameRate }.toInt(),
          videoStabilizationModes = videoStabilizationModes,
          metadata = hashMapOf(DEVICE_FORMAT to format),
        )
      }
    }
}
