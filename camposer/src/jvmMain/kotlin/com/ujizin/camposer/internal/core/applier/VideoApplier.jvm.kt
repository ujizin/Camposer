package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.internal.core.jvm.JvmCameraController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.jpegQuality

internal actual class VideoApplier(
  private val cameraState: CameraState,
  private val jvmCameraController: JvmCameraController,
) : CameraStateApplier {
  override fun onCameraInitialized() {
    jvmCameraController.setPictureQuality(cameraState.imageCaptureStrategy.value.jpegQuality)
  }

  actual fun applyImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy) {
    jvmCameraController.setPictureQuality(imageCaptureStrategy.jpegQuality)
    cameraState.updateImageCaptureStrategy(imageCaptureStrategy)
  }

  // Runs off the caller thread: the hardware write waits for the capture lock.
  actual fun applyFrameRate(frameRate: Int) {
    cameraState.launch {
      jvmCameraController.setFrameRate(frameRate)
      cameraState.updateFrameRate(frameRate)
    }
  }

  /** Desktop backends expose no stabilization control. The value is stored in state only. */
  actual fun applyVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode) {
    cameraState.updateVideoStabilizationMode(videoStabilizationMode)
  }
}
