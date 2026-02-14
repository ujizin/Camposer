package com.ujizin.camposer.shared.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFoundation.AVAsset
import platform.AVFoundation.AVAssetImageGenerator
import platform.CoreGraphics.CGImageRelease
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
public actual suspend fun getFirstFrameVideo(filename: String): ByteArray {
  val videoUrl = NSURL.fileURLWithPath(filename)
  val asset = AVAsset.assetWithURL(videoUrl)
  val imageGenerator = AVAssetImageGenerator(asset)
  imageGenerator.appliesPreferredTrackTransform = true

  val cgImage = imageGenerator.copyCGImageAtTime(
    CMTimeMakeWithSeconds(0.0, 600),
    actualTime = null,
    error = null,
  ) ?: error("Could not extract first frame from video: $filename")

  val uiImage = UIImage.imageWithCGImage(cgImage)
  CGImageRelease(cgImage)

  val frameData = UIImageJPEGRepresentation(uiImage, 1.0)
    ?: error("Could not encode first frame from video: $filename")

  return frameData.toByteArray()
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray =
  ByteArray(length.toInt()).apply {
    usePinned {
      memcpy(it.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
    }
  }
