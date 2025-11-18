package com.ujizin.camposer.state.properties.format

import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.format.config.CameraFormatConfig
import platform.AVFoundation.AVCaptureDeviceFormat

public actual class CamFormat actual constructor(
    vararg configs: CameraFormatConfig,
) {

    public actual constructor() : this(*Default.configs.toTypedArray())

    public actual val configs: List<CameraFormatConfig> = configs.toList()

    internal fun getDeviceFormat(cameraInfo: CameraInfo): AVCaptureDeviceFormat? {
        val cameraData = CameraFormatPicker.selectBestFormatByOrder(configs, cameraInfo.allFormats)
        val format = cameraData?.metadata?.get(CameraData.DEVICE_FORMAT) as? AVCaptureDeviceFormat

        return format
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CamFormat

        return configs == other.configs
    }

    override fun hashCode(): Int {
        return configs.hashCode()
    }

    override fun toString(): String {
        return "CameraResolution(cameraData=$configs)"
    }

    public actual companion object
}
