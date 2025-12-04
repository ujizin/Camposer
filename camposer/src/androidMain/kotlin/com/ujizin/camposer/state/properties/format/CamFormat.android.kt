package com.ujizin.camposer.state.properties.format

import android.util.Size
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.view.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.utils.Debouncer
import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.config.AspectRatioConfig
import com.ujizin.camposer.state.properties.format.config.CameraFormatConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import kotlinx.coroutines.MainScope
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

public actual class CamFormat
  actual constructor(
    vararg configs: CameraFormatConfig,
  ) {
    public actual constructor() : this(*Default.configs.toTypedArray())

    public actual val configs: List<CameraFormatConfig> = configs.toList()

    private val debouncer = Debouncer(CONFIGS_DEBOUNCE_MILLIS.milliseconds, MainScope())

    private val qualitySizes =
      mapOf(
        Quality.UHD to 3840 * 2160,
        Quality.FHD to 1920 * 1080,
        Quality.HD to 1280 * 720,
        Quality.SD to 720 * 480,
      )

    internal fun applyConfigs(
      cameraInfo: CameraInfo,
      controller: CameraController,
      onFrameRateChanged: (Int) -> Unit,
      onStabilizationModeChanged: (VideoStabilizationMode) -> Unit,
    ) = debouncer.submit {
      val resolutionSelector = getResolutionSelector()
      with(controller) {
        applyBestVideoCamFormat(
          videoFormats = cameraInfo.videoFormats,
          onFrameRateChanged = onFrameRateChanged,
          onStabilizationModeChanged = onStabilizationModeChanged,
        )

        imageCaptureResolutionSelector = resolutionSelector
        imageAnalysisResolutionSelector = resolutionSelector
        previewResolutionSelector = resolutionSelector
      }
    }

    private fun CameraController.applyBestVideoCamFormat(
      videoFormats: List<CameraData>,
      onFrameRateChanged: (Int) -> Unit,
      onStabilizationModeChanged: (VideoStabilizationMode) -> Unit,
    ) {
      CameraFormatPicker.selectBestFormatByOrder(
        formats = videoFormats,
        configs = configs,
        onFormatChanged = { videoCaptureQualitySelector = it.toSize().getQualitySelector() },
        onFrameRateChanged = onFrameRateChanged,
        onStabilizationModeChanged = onStabilizationModeChanged,
      )
    }

    private fun getResolutionSelector() =
      ResolutionSelector
        .Builder()
        .setResolutionFilter { sizes, _ ->
          val formats = sizes.map { CameraData(it.width, it.height) }
          val selectSize =
            CameraFormatPicker
              .getBestFormatByOrder(
                formats = formats,
                configs =
                  configs.filter { it is ResolutionConfig || it is AspectRatioConfig },
              )?.toSize()

          val selectedSizes = listOfNotNull(selectSize)
          selectedSizes + (sizes - selectedSizes.toSet())
        }.build()

    private fun Size.getQualitySelector(): QualitySelector {
      val (quality, _) = qualitySizes.minBy { abs((width * height) - it.value) }
      return QualitySelector.from(quality, FallbackStrategy.lowerQualityOrHigherThan(quality))
    }

    private fun CameraData.toSize() = Size(width, height)

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is CamFormat) return false
      return configs == other.configs
    }

    override fun hashCode(): Int = configs.hashCode()

    override fun toString(): String = "CamFormat(configs=$configs)"

    public actual companion object {
      private const val CONFIGS_DEBOUNCE_MILLIS = 250L
    }
  }
