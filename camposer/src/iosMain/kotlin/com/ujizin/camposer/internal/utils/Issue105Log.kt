package com.ujizin.camposer.internal.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVFrameRateRange
import platform.CoreMedia.CMVideoFormatDescriptionGetDimensions
import platform.Foundation.NSLog

// TEMP: test logs for issue #105. Do not merge.
@OptIn(ExperimentalForeignApi::class)
internal fun logIssue105(
  step: String,
  session: AVCaptureSession,
  device: AVCaptureDevice?,
) {
  val format = device?.activeFormat
  val dimensions = format
    ?.let { CMVideoFormatDescriptionGetDimensions(it.formatDescription) }
    ?.useContents { "${width}x$height" }
  val maxFps = format?.videoSupportedFrameRateRanges
    ?.filterIsInstance<AVFrameRateRange>()
    ?.maxOfOrNull { it.maxFrameRate }
  NSLog(
    "[#105] $step: activeFormat=$dimensions maxFps=$maxFps " +
      "preset=${session.sessionPreset} running=${session.isRunning()}",
  )
}

internal fun logIssue105(message: String) {
  NSLog("[#105] $message")
}
