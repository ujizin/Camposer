package com.ujizin.camposer.state.properties.format

import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.config.AspectRatioConfig
import com.ujizin.camposer.state.properties.format.config.CameraFormatConfig
import com.ujizin.camposer.state.properties.format.config.FrameRateConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import com.ujizin.camposer.state.properties.format.config.VideoStabilizationConfig
import kotlin.math.abs
import kotlin.math.pow

internal object CameraFormatPicker {

    internal fun selectBestFormatByOrder(
        configs: List<CameraFormatConfig>,
        formats: List<CameraData>,
    ): CameraData? {
        if (formats.isEmpty()) return null
        val format = formats.minBy { getFinalScoreByOrder(it, configs) }
        return format
    }

    private fun getFinalScoreByOrder(
        format: CameraData,
        priorities: List<CameraFormatConfig>,
    ): Double {
        var score = 0.0

        val total = priorities.size
        for ((index, config) in priorities.withIndex()) {
            val weight = getPositionalWeight(index, total)
            score += when (config) {
                is AspectRatioConfig -> getAspectRatioDistance(format, config)
                is ResolutionConfig -> getResolutionDistance(format, config)
                is FrameRateConfig -> getFpsDistance(format, config.maxFps)
                is VideoStabilizationConfig -> getStabilizationDistance(format, config.mode)
                else -> 0F
            } * weight
        }

        score -= (format.width.toLong() * format.height.toLong()).toDouble() / 100_000.0

        return score
    }

    private fun getAspectRatioDistance(
        format: CameraData,
        desired: AspectRatioConfig,
    ): Float {
        val cameraAspectRatio = format.width.toFloat() / format.height.toFloat()
        return abs(cameraAspectRatio - desired.aspectRatio)
    }

    private fun getResolutionDistance(format: CameraData, desired: ResolutionConfig): Float {
        return (abs(format.width - desired.width) + abs(format.height - desired.height)) / 1000F
    }

    private fun getFpsDistance(format: CameraData, fps: Int): Float {
        return when {
            fps in (format.minFps ?: 0)..(format.maxFps ?: 0) -> 0F
            fps < (format.minFps ?: 0) -> ((format.minFps ?: 0) - fps).toFloat()
            else -> (fps - (format.maxFps ?: 0)).toFloat()
        }
    }

    private fun getStabilizationDistance(
        format: CameraData,
        desiredMode: VideoStabilizationMode?,
    ): Float {
        val modes = format.videoStabilizationModes ?: return 0F
        return when {
            desiredMode == null -> if (modes.any { it != VideoStabilizationMode.Off }) 0F else 1F
            modes.contains(desiredMode) -> 0F
            modes.any { it != VideoStabilizationMode.Off } -> 0.5F
            else -> 1.0F
        }
    }

    private fun getPositionalWeight(index: Int, total: Int, base: Double = 10.0): Double {
        val exponent = (total - index).toDouble()
        return base.pow(exponent)
    }
}