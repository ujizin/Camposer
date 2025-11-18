package com.ujizin.camposer.state.properties.format

import android.util.Range
import android.util.Size
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.view.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.format.config.AspectRatioConfig
import com.ujizin.camposer.state.properties.format.config.CameraFormatConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import kotlin.math.abs

public actual class CamFormat actual constructor(
    vararg configs: CameraFormatConfig,
) {

    public actual constructor() : this(*Default.configs.toTypedArray())

    public actual val configs: List<CameraFormatConfig> = configs.toList()

    private val qualitySizes = mapOf(
        Quality.UHD to 3840 * 2160,
        Quality.FHD to 1920 * 1080,
        Quality.HD to 1280 * 720,
        Quality.SD to 720 * 480,
    )

    internal fun applyConfigs(cameraInfo: CameraInfo, controller: CameraController) {
        controller.applyBestImageCamFormat()
        if (controller.isVideoCaptureEnabled) {
            controller.applyBestVideoCamFormat(cameraInfo.videoFormats)
        }
    }

    private fun CameraController.applyBestVideoCamFormat(videoFormats: List<CameraData>) {
        val videoCameraData = CameraFormatPicker.selectBestFormatByOrder(
            formats = videoFormats,
            configs = configs,
        ) ?: return

        videoCaptureTargetFrameRate = videoCameraData.getRangeFps() ?: videoCaptureTargetFrameRate
        videoCaptureQualitySelector = videoCameraData.toSize().getQualitySelector()
    }

    private fun CameraController.applyBestImageCamFormat() {
        val resolutionSelector = ResolutionSelector.Builder().setResolutionFilter { sizes, _ ->
            val formats = sizes.map { CameraData(it.width, it.height) }
            val selectSize = CameraFormatPicker.selectBestFormatByOrder(
                formats = formats,
                configs = configs.filter { it is ResolutionConfig || it is AspectRatioConfig },
            )?.toSize()

            val selectedSizes = listOfNotNull(selectSize)
            selectedSizes + (sizes - selectedSizes)
        }.build()

        imageCaptureResolutionSelector = resolutionSelector
        previewResolutionSelector = resolutionSelector
    }

    private fun Size.getQualitySelector(): QualitySelector {
        val (quality, _) = qualitySizes.minBy { abs((width * height) - it.value) }
        return QualitySelector.from(quality, FallbackStrategy.lowerQualityOrHigherThan(quality))
    }

    private fun CameraData.toSize() = Size(width, height)

    private fun CameraData.getRangeFps(): Range<Int>? {
        if (minFps == null || maxFps == null) return null
        return Range(minFps, maxFps)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CamFormat) return false
        return configs == other.configs
    }

    override fun hashCode(): Int {
        return configs.hashCode()
    }

    override fun toString(): String {
        return "CamFormat(configs=$configs)"
    }

    public actual companion object
}

