package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.highResolutionEnabled
import com.ujizin.camposer.state.properties.quality
import com.ujizin.camposer.state.properties.value

internal class VideoApplier(
  private val cameraState: CameraState,
  private val iOSCameraController: IOSCameraController,
) : CameraStateApplier {
  fun applyImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy) {
    cameraState.launch {
      iOSCameraController.setCameraOutputQuality(
        quality = imageCaptureStrategy.quality,
        highResolutionEnabled = imageCaptureStrategy.highResolutionEnabled,
      )
      cameraState.updateImageCaptureStrategy(imageCaptureStrategy)
    }
  }

  fun applyFrameRate(frameRate: Int) {
    cameraState.launch {
      iOSCameraController.setFrameRate(frameRate)
      cameraState.updateFrameRate(frameRate)
    }
  }

  fun applyVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode) {
    iOSCameraController.setVideoStabilization(videoStabilizationMode.value)
    cameraState.updateVideoStabilizationMode(videoStabilizationMode)
  }
}
