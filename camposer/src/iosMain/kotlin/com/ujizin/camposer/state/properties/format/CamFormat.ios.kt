package com.ujizin.camposer.state.properties.format

import androidx.compose.runtime.Stable
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.CameraFormatPicker.selectBestFormatByOrder
import com.ujizin.camposer.state.properties.format.config.CameraFormatConfig
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDeviceFormat

@Stable
public actual class CamFormat actual constructor(
  vararg configs: CameraFormatConfig,
) {
  public actual constructor() : this(*Default.configs.toTypedArray())

  public actual val configs: List<CameraFormatConfig> = configs.toList()

  @OptIn(ExperimentalForeignApi::class)
  internal fun applyConfigs(
    cameraInfo: CameraInfo,
    iosCameraController: IOSCameraController,
    onDeviceFormatUpdated: () -> Unit,
    onStabilizationModeChanged: (VideoStabilizationMode) -> Unit,
    onFrameRateChanged: (Int) -> Unit,
  ) = selectBestFormatByOrder(
    configs = configs,
    formats = cameraInfo.allFormats,
    onFormatChanged = { cameraData ->
      val format = cameraData.metadata[CameraData.DEVICE_FORMAT] as AVCaptureDeviceFormat
      iosCameraController.setDeviceFormat(format)
      onDeviceFormatUpdated()
    },
    onFrameRateChanged = onFrameRateChanged,
    onStabilizationModeChanged = onStabilizationModeChanged,
  )

  actual override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as CamFormat

    return configs == other.configs
  }

  actual override fun hashCode(): Int = configs.hashCode()

  actual override fun toString(): String = "CameraResolution(cameraData=$configs)"

  public actual companion object
}
