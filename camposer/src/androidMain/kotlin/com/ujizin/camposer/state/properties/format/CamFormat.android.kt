package com.ujizin.camposer.state.properties.format

import android.util.Log
import android.util.Size
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.camerax.CameraXController
import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.config.AspectRatioConfig
import com.ujizin.camposer.state.properties.format.config.CameraFormatConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import kotlin.math.abs

public actual class CamFormat
  actual constructor(
    vararg configs: CameraFormatConfig,
  ) {
    public actual constructor() : this(*Default.configs.toTypedArray())

    public actual val configs: List<CameraFormatConfig> = configs.toList()

    private val qualitySelectorSizes = mapOf(
      Quality.UHD.toQualitySelector() to 3840 * 2160,
      Quality.FHD.toQualitySelector() to 1920 * 1080,
      Quality.HD.toQualitySelector() to 1280 * 720,
      Quality.SD.toQualitySelector() to 720 * 480,
    )

    @VisibleForTesting(PRIVATE)
    internal val resolutionSelector = ResolutionSelector
      .Builder()
      .setResolutionFilter { sizes, _ ->
        val formats = sizes.map { CameraData(it.width, it.height) }
        val selectSize =
          CameraFormatPicker
            .getBestFormatByOrder(
              formats = formats,
              configs = configs.filter { it is ResolutionConfig || it is AspectRatioConfig },
            )?.toSize()

        val selectedSizes = listOfNotNull(selectSize)

        Log.i("YUJI", "Selected size: $selectedSizes")

        selectedSizes + (sizes - selectedSizes.toSet())
      }.build()

    internal fun applyConfigs(
      cameraInfo: CameraInfo,
      controller: CameraXController,
      onFrameRateChanged: (Int) -> Unit,
      onStabilizationModeChanged: (VideoStabilizationMode) -> Unit,
    ) {
      with(controller) {
        unbind()

        previewResolutionSelector = resolutionSelector
        imageCaptureResolutionSelector = resolutionSelector
        imageAnalysisResolutionSelector = resolutionSelector

        applyBestVideoCamFormat(
          videoFormats = cameraInfo.videoFormats,
          onFrameRateChanged = onFrameRateChanged,
          onStabilizationModeChanged = onStabilizationModeChanged,
        )

        bindToLifecycle(lifecycleOwner)
      }
    }

    private fun CameraXController.applyBestVideoCamFormat(
      videoFormats: List<CameraData>,
      onFrameRateChanged: (Int) -> Unit,
      onStabilizationModeChanged: (VideoStabilizationMode) -> Unit,
    ) {
      CameraFormatPicker.selectBestFormatByOrder(
        formats = videoFormats,
        configs = configs,
        onFormatChanged = {
          videoCaptureQualitySelector = getQualitySelector(it.toSize())
        },
        onFrameRateChanged = onFrameRateChanged,
        onStabilizationModeChanged = onStabilizationModeChanged,
      )
    }

    private fun getQualitySelector(size: Size): QualitySelector {
      val (quality, _) = qualitySelectorSizes.minBy { abs((size.width * size.height) - it.value) }
      return quality
    }

    private fun Quality.toQualitySelector(): QualitySelector =
      QualitySelector.from(
        this,
        FallbackStrategy.lowerQualityOrHigherThan(this),
      )

    private fun CameraData.toSize() = Size(width, height)

    actual override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is CamFormat) return false
      return configs == other.configs
    }

    actual override fun hashCode(): Int = configs.hashCode()

    actual override fun toString(): String = "CamFormat(configs=$configs)"

    public actual companion object
  }
