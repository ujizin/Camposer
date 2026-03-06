package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.internal.capture.JvmCameraCapture
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FPS

internal actual class VideoApplier(
  private val cameraState: CameraState,
  private val capture: JvmCameraCapture,
) : CameraStateApplier {
  fun applyImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy) {
    cameraState.updateImageCaptureStrategy(imageCaptureStrategy)
  }

  fun applyFrameRate(frameRate: Int) {
    capture.set(CAP_PROP_FPS, frameRate.toDouble())
    cameraState.updateFrameRate(frameRate)
  }

  fun applyVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode) {
    cameraState.updateVideoStabilizationMode(videoStabilizationMode)
  }
}
