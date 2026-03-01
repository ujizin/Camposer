package com.ujizin.camposer.state.properties.format

import androidx.compose.runtime.Stable
import com.ujizin.camposer.internal.capture.JvmCameraCapture
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.config.CameraFormatConfig
import com.ujizin.camposer.state.properties.format.config.FrameRateConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FPS
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_HEIGHT
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_WIDTH

@Stable
public actual class CamFormat actual constructor(
  vararg configs: CameraFormatConfig,
) {
  public actual constructor() : this(*Default.configs.toTypedArray())

  public actual val configs: List<CameraFormatConfig> = configs.toList()

  internal fun applyConfigs(
    capture: JvmCameraCapture,
    onFrameRateChanged: (Int) -> Unit,
    onStabilizationModeChanged: (VideoStabilizationMode) -> Unit,
  ) {
    for (config in configs) {
      when (config) {
        is FrameRateConfig -> {
          capture.set(CAP_PROP_FPS, config.fps.toDouble())
          onFrameRateChanged(config.fps)
        }
        is ResolutionConfig -> {
          capture.set(CAP_PROP_FRAME_WIDTH, config.width.toDouble())
          capture.set(CAP_PROP_FRAME_HEIGHT, config.height.toDouble())
        }
        else -> {
          // AspectRatioConfig, VideoStabilizationConfig, and other configs
          // are not applicable to JVM camera capture — no-op.
        }
      }
    }
  }

  actual override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as CamFormat

    return configs == other.configs
  }

  actual override fun hashCode(): Int = configs.hashCode()

  actual override fun toString(): String = "CamFormat(configs=$configs)"

  public actual companion object
}
