package com.ujizin.camposer.state.properties.format

import com.ujizin.camposer.internal.extensions.firstIsInstanceOrNull
import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.config.AspectRatioConfig
import com.ujizin.camposer.state.properties.format.config.CameraFormatConfig
import com.ujizin.camposer.state.properties.format.config.FrameRateConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import com.ujizin.camposer.state.properties.format.config.VideoStabilizationConfig
import kotlin.math.abs
import kotlin.math.pow

private const val RESOLUTION_NORMALIZATION = 1000F
private const val MEGAPIXEL_DIVISOR = 1_000_000.0
private const val FPS_PARTIAL_MATCH_PENALTY = 0.5F
private const val STABILIZATION_SCORE_MAX = 0.6F
private const val STABILIZATION_SCORE_DENOMINATOR = 10F
private const val STABILIZATION_SCORE_OFFSET = 0.1F

internal object CameraFormatPicker {
  internal fun getBestFormatByOrder(
    configs: List<CameraFormatConfig>,
    formats: List<CameraData>,
  ): CameraData? {
    if (formats.isEmpty()) return null
    val cameraData = formats.minBy { getFinalScoreByOrder(it, configs) }
    return cameraData
  }

  internal fun selectBestFormatByOrder(
    configs: List<CameraFormatConfig>,
    formats: List<CameraData>,
    onFormatChanged: (CameraData) -> Unit,
    onFrameRateChanged: (Int) -> Unit,
    onStabilizationModeChanged: (VideoStabilizationMode) -> Unit,
  ) {
    val cameraData = getBestFormatByOrder(configs, formats) ?: return

    onFormatChanged(cameraData)

    // Video stabilization
    val stabilizationMode = cameraData.getStabilizationModeByConfigs(configs)
    runCatching { onStabilizationModeChanged(stabilizationMode) }

    // FPS
    val fps = cameraData.getFrameRateByConfigs(configs)
    runCatching { onFrameRateChanged(fps) }
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
        is FrameRateConfig -> getFpsDistance(format, config.fps)
        is VideoStabilizationConfig -> getStabilizationDistance(format, config.mode)
      } * weight
    }

    score -= (format.width.toLong() * format.height.toLong()).toDouble() / MEGAPIXEL_DIVISOR

    return score
  }

  private fun getAspectRatioDistance(
    format: CameraData,
    desired: AspectRatioConfig,
  ): Float {
    val cameraAspectRatio = format.width.toFloat() / format.height.toFloat()
    return abs(cameraAspectRatio - desired.aspectRatio)
  }

  private fun getResolutionDistance(
    format: CameraData,
    desired: ResolutionConfig,
  ): Float =
    (abs(format.width - desired.width) + abs(format.height - desired.height)) /
      RESOLUTION_NORMALIZATION

  private fun getFpsDistance(
    format: CameraData,
    fps: Int,
  ): Float {
    val minFps = format.minFps ?: 0
    val maxFps = format.maxFps ?: 0
    return when {
      fps in minFps..maxFps -> when {
        minFps == fps || maxFps == fps -> 0F
        else -> FPS_PARTIAL_MATCH_PENALTY
      }

      fps < minFps -> (minFps - fps).toFloat()

      else -> (fps - maxFps).toFloat()
    }
  }

  private fun getStabilizationDistance(
    format: CameraData,
    desiredMode: VideoStabilizationMode?,
  ): Float {
    val modes = format.videoStabilizationModes ?: return 1F
    return when {
      desiredMode == null -> {
        if (modes.any { it != VideoStabilizationMode.Off }) 0F else 1F
      }

      modes.contains(desiredMode) -> {
        (modes.size / STABILIZATION_SCORE_DENOMINATOR).coerceAtMost(STABILIZATION_SCORE_MAX) -
          STABILIZATION_SCORE_OFFSET
      }

      modes.any { it != VideoStabilizationMode.Off } -> {
        FPS_PARTIAL_MATCH_PENALTY
      }

      else -> {
        1.0F
      }
    }
  }

  private fun getPositionalWeight(
    index: Int,
    total: Int,
    base: Double = 100.0,
  ): Double {
    val exponent = (total - index).toDouble()
    return base.pow(exponent)
  }
}
