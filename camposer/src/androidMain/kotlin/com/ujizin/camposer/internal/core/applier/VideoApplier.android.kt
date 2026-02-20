package com.ujizin.camposer.internal.core.applier

import android.util.Range
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalZeroShutterLag
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.camerax.CameraXController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.ImageCaptureStrategy.MinLatency
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.fallback
import com.ujizin.camposer.state.properties.mode

internal class VideoApplier(
  private val cameraState: CameraState,
  private val cameraInfo: CameraInfo,
  private val cameraXController: CameraXController,
) : CameraStateApplier {
  fun applyImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy) {
    setImageCaptureStrategy(imageCaptureStrategy)
    cameraState.updateImageCaptureStrategy(imageCaptureStrategy)
  }

  fun applyFrameRate(frameRate: Int) {
    setFrameRate(frameRate)
    cameraState.updateFrameRate(frameRate)
  }

  fun applyVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode) {
    setVideoStabilizationMode(videoStabilizationMode)
    cameraState.updateVideoStabilizationMode(videoStabilizationMode)
  }

  @OptIn(ExperimentalZeroShutterLag::class)
  private fun setImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy) {
    val isZSLSupported = cameraInfo.state.value.isZeroShutterLagSupported
    val mode = when {
      imageCaptureStrategy == MinLatency && !isZSLSupported -> imageCaptureStrategy.fallback
      else -> imageCaptureStrategy.mode
    }
    cameraXController.imageCaptureMode = mode
  }

  private fun setFrameRate(frameRate: Int) {
    cameraXController.videoCaptureTargetFrameRate = Range(frameRate, frameRate)
  }

  private fun setVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode) {
    // TODO CameraX controller does not support yet :(
  }
}
