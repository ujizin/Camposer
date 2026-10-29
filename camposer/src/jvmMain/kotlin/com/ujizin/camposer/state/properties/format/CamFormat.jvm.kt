package com.ujizin.camposer.state.properties.format

import androidx.compose.runtime.Stable
import com.ujizin.camposer.internal.core.jvm.JvmCameraController
import com.ujizin.camposer.internal.extensions.firstIsInstanceOrNull
import com.ujizin.camposer.internal.utils.JvmCameraFormats
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.CameraFormatPicker.selectBestFormatByOrder
import com.ujizin.camposer.state.properties.format.config.CameraFormatConfig
import com.ujizin.camposer.state.properties.format.config.FrameRateConfig

private const val DEFAULT_FRAME_RATE = 30

@Stable
public actual class CamFormat actual constructor(
  vararg configs: CameraFormatConfig,
) {
  public actual constructor() : this(*Default.configs.toTypedArray())

  public actual val configs: List<CameraFormatConfig> = configs.toList()

  internal fun applyConfigs(
    jvmCameraController: JvmCameraController,
    onFrameRateChanged: (Int) -> Unit,
    onStabilizationModeChanged: (VideoStabilizationMode) -> Unit,
  ) {
    val currentFrameRate = jvmCameraController.currentFormat?.maxFps ?: DEFAULT_FRAME_RATE
    val requestedFrameRate = configs.firstIsInstanceOrNull<FrameRateConfig>()?.fps
    selectBestFormatByOrder(
      configs = configs,
      formats = JvmCameraFormats.candidates(currentFrameRate),
      onFormatChanged = { cameraData ->
        jvmCameraController.setFormat(width = cameraData.width, height = cameraData.height)
      },
      // Desktop backends cannot report supported frame rates. Only change the rate when the
      // format asks for one; otherwise keep what the backend already delivers.
      onFrameRateChanged = { requestedFrameRate?.let(onFrameRateChanged) },
      onStabilizationModeChanged = onStabilizationModeChanged,
    )
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
