package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.VideoStabilizationMode

internal expect class VideoApplier : CameraStateApplier {
  fun applyImageCaptureStrategy(imageCaptureStrategy: ImageCaptureStrategy)
  fun applyFrameRate(frameRate: Int)
  fun applyVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode)
}
