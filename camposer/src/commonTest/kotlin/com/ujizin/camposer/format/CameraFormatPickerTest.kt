package com.ujizin.camposer.format

import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.CameraFormatPicker
import com.ujizin.camposer.state.properties.format.CameraFormatPicker.getBestFormatByOrder
import com.ujizin.camposer.state.properties.format.config.AspectRatioConfig
import com.ujizin.camposer.state.properties.format.config.FrameRateConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import com.ujizin.camposer.state.properties.format.config.VideoStabilizationConfig
import com.ujizin.camposer.utils.CamFormatConfigUtils.convertResolutionConfig
import com.ujizin.camposer.utils.CamFormatConfigUtils.defaultConfigs
import com.ujizin.camposer.utils.CameraDataUtils.cameraDataDefault
import com.ujizin.camposer.utils.CameraDataUtils.cameraDataHigh16X9_4KResolution
import com.ujizin.camposer.utils.CameraDataUtils.cameraDataHigh4X3_4KResolution
import com.ujizin.camposer.utils.CameraDataUtils.cameraDataHighResolution
import com.ujizin.camposer.utils.CameraDataUtils.cameraDataLowResolution
import com.ujizin.camposer.utils.CameraDataUtils.cameraDataMediumResolution
import com.ujizin.camposer.utils.CameraDataUtils.cameraDataStabilizationList
import com.ujizin.camposer.utils.CameraDataUtils.cameraDataStandardList
import com.ujizin.camposer.utils.CameraDataUtils.cameraDataVideoStabilizationHigh
import com.ujizin.camposer.utils.CameraDataUtils.cameraDataVideoStabilizationHighCinematic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CameraFormatPickerTest {
  @Test
  fun getBestFormatByOrder_returns_null_for_empty_formats() {
    val result = getBestFormatByOrder(configs = defaultConfigs, formats = emptyList())
    assertNull(result)
  }

  @Test
  fun getBestFormatByOrder_returns_single_available_format() {
    val formats = listOf(cameraDataDefault)
    val result = getBestFormatByOrder(configs = defaultConfigs, formats = formats)

    assertEquals(cameraDataDefault, result)
  }

  @Test
  fun getBestFormatByOrder_prioritizes_higher_resolution_when_no_config() {
    val formats = cameraDataStandardList
    val result = getBestFormatByOrder(configs = emptyList(), formats = formats)

    assertEquals(cameraDataHighResolution, result)
  }

  @Test
  fun getBestFormatByOrder_prioritizes_medium_resolution_when_medium_is_priority() {
    val formats = cameraDataStandardList
    val result = getBestFormatByOrder(
      configs = listOf(convertResolutionConfig(cameraDataMediumResolution)),
      formats = formats,
    )

    assertEquals(cameraDataMediumResolution, result)
  }

  @Test
  fun getBestFormatByOrder_prioritizes_low_resolution_when_low_is_priority() {
    val formats = cameraDataStandardList
    val result = getBestFormatByOrder(
      configs = listOf(convertResolutionConfig(cameraDataLowResolution)),
      formats = formats,
    )

    assertEquals(cameraDataLowResolution, result)
  }

  @Test
  fun getBestFormatByOrder_respects_High_Resolution_priority_and_Aspect_ratio_4x3() {
    val result = getBestFormatByOrder(
      configs = listOf(
        convertResolutionConfig(cameraDataHighResolution),
        AspectRatioConfig(aspectRatio = 4F / 3F),
      ),
      formats = cameraDataStandardList,
    )

    assertEquals(cameraDataHighResolution, result)
  }

  @Test
  fun getBestFormatByOrder_respects_AspectRatioConfig_16x9_priority() {
    val result = getBestFormatByOrder(
      configs = listOf(AspectRatioConfig(aspectRatio = 16F / 9F)),
      formats = cameraDataStandardList,
    )

    assertEquals(cameraDataHighResolution, result)
  }

  @Test
  fun getBestFormatByOrder_respects_AspectRatioConfig_16x9_priority_and_Medium_Resolution() {
    val result = getBestFormatByOrder(
      configs = listOf(
        AspectRatioConfig(aspectRatio = 16F / 9F),
        convertResolutionConfig(cameraDataMediumResolution),
      ),
      formats = cameraDataStandardList,
    )

    assertEquals(cameraDataMediumResolution, result)
  }

  @Test
  fun getBestFormatByOrder_respects_AspectRatioConfig_4x3_priority() {
    val result = getBestFormatByOrder(
      configs = listOf(AspectRatioConfig(aspectRatio = 4F / 3F)),
      formats = cameraDataStandardList,
    )

    assertEquals(cameraDataLowResolution, result)
  }

  @Test
  fun getBestFormatByOrder_respects_AspectRatioConfig_4x3_priority_and_High_Resolution() {
    val result = getBestFormatByOrder(
      configs = listOf(
        AspectRatioConfig(aspectRatio = 4F / 3F),
        convertResolutionConfig(cameraDataHighResolution),
      ),
      formats = cameraDataStandardList,
    )

    assertEquals(cameraDataLowResolution, result)
  }

  @Test
  fun getBestFormatByOrder_respects_FrameRateConfig_60_priority() {
    val result = getBestFormatByOrder(
      configs = listOf(FrameRateConfig(60)),
      formats = cameraDataStandardList,
    )

    assertEquals(cameraDataHighResolution, result)
  }

  @Test
  fun getBestFormatByOrder_respects_FrameRateConfig_30_priority() {
    val result = getBestFormatByOrder(
      configs = listOf(FrameRateConfig(30)),
      formats = cameraDataStandardList,
    )

    assertEquals(cameraDataMediumResolution, result)
  }

  @Test
  fun getBestFormatByOrder_respects_FrameRateConfig_24_priority() {
    val result = getBestFormatByOrder(
      configs = listOf(FrameRateConfig(24)),
      formats = cameraDataStandardList,
    )

    assertEquals(cameraDataLowResolution, result)
  }

  @Test
  fun getBestFormatByOrder_respects_VideoStabilizationConfig_Cinematic_EE_priority() {
    val result = getBestFormatByOrder(
      configs = listOf(VideoStabilizationConfig(VideoStabilizationMode.CinematicExtendedEnhanced)),
      formats = cameraDataStabilizationList,
    )

    assertEquals(cameraDataVideoStabilizationHighCinematic, result)
  }

  @Test
  fun getBestFormatByOrder_respects_VideoStabilizationConfig_Standard_priority() {
    val result = getBestFormatByOrder(
      configs = listOf(VideoStabilizationConfig(VideoStabilizationMode.Standard)),
      formats = cameraDataStabilizationList,
    )

    assertEquals(cameraDataVideoStabilizationHigh, result)
  }

  @Test
  fun getBestFormatByOrder_config_order_weighting_Aspect_vs_Resolution() {
    // Case A: [AspectRatio(4:3), Resolution(High)] -> Priority to Aspect Ratio 4:3 (Low Res 4:3 > High Res 16:9)
    val resultA = getBestFormatByOrder(
      configs = listOf(
        AspectRatioConfig(aspectRatio = 4F / 3F),
        convertResolutionConfig(cameraDataHighResolution),
      ),
      formats = cameraDataStandardList,
    )

    assertEquals(cameraDataLowResolution, resultA)

    // Case B: [Resolution(High), AspectRatio(4:3)] -> Priority to Resolution High (High Res 16:9 > Low Res 4:3)
    val resultB = getBestFormatByOrder(
      configs = listOf(
        convertResolutionConfig(cameraDataHighResolution),
        AspectRatioConfig(aspectRatio = 4F / 3F),
      ),
      formats = cameraDataStandardList,
    )
    assertEquals(cameraDataHighResolution, resultB)
  }

  @Test
  fun getBestFormatByOrder_config_order_weighting_Aspect_vs_Resolution_vs_Frame_Rate() {
    val formats = listOf(
      cameraDataHighResolution,
      cameraDataVideoStabilizationHighCinematic,
      cameraDataHigh16X9_4KResolution,
      cameraDataHigh4X3_4KResolution,
    )

    // Aspect ratio 4x3, 4k resolution
    listOf(
      getBestFormatByOrder(
        configs = listOf(
          AspectRatioConfig(aspectRatio = 4F / 3F),
          ResolutionConfig(3000, 2000),
          FrameRateConfig(60),
          VideoStabilizationConfig(VideoStabilizationMode.CinematicExtendedEnhanced),
        ),
        formats = formats,
      ),
      getBestFormatByOrder(
        configs = listOf(
          AspectRatioConfig(aspectRatio = 4F / 3F),
          ResolutionConfig(3000, 2000),
          FrameRateConfig(60),
          VideoStabilizationConfig(VideoStabilizationMode.CinematicExtendedEnhanced),
        ),
        formats = formats,
      ),
    ).forEach { assertEquals(cameraDataHigh4X3_4KResolution, it) }

    // Aspect ratio 16x9, 4k resolution
    listOf(
      getBestFormatByOrder(
        configs = listOf(
          AspectRatioConfig(aspectRatio = 16F / 9F),
          ResolutionConfig(3000, 2000),
          FrameRateConfig(60),
          VideoStabilizationConfig(VideoStabilizationMode.CinematicExtendedEnhanced),
        ),
        formats = formats,
      ),
      getBestFormatByOrder(
        configs = listOf(
          ResolutionConfig(3000, 2000),
          AspectRatioConfig(aspectRatio = 16F / 9F),
          FrameRateConfig(60),
          VideoStabilizationConfig(VideoStabilizationMode.CinematicExtendedEnhanced),
        ),
        formats = formats,
      ),
    ).forEach { assertEquals(cameraDataHigh16X9_4KResolution, it) }

    // Frame rate + Cinematic
    listOf(
      getBestFormatByOrder(
        configs = listOf(
          FrameRateConfig(144),
          ResolutionConfig(3000, 2000),
          AspectRatioConfig(aspectRatio = 16F / 9F),
          VideoStabilizationConfig(VideoStabilizationMode.CinematicExtendedEnhanced),
        ),
        formats = formats,
      ),
      getBestFormatByOrder(
        configs = listOf(
          FrameRateConfig(120),
          ResolutionConfig(3000, 2000),
          AspectRatioConfig(aspectRatio = 16F / 9F),
          VideoStabilizationConfig(VideoStabilizationMode.CinematicExtendedEnhanced),
        ),
        formats = formats,
      ),
      getBestFormatByOrder(
        configs = listOf(
          FrameRateConfig(60),
          ResolutionConfig(3000, 2000),
          AspectRatioConfig(aspectRatio = 16F / 9F),
          VideoStabilizationConfig(VideoStabilizationMode.CinematicExtendedEnhanced),
        ),
        formats = formats,
      ),
      getBestFormatByOrder(
        configs = listOf(
          FrameRateConfig(60),
          AspectRatioConfig(aspectRatio = 16F / 9F),
          VideoStabilizationConfig(VideoStabilizationMode.CinematicExtendedEnhanced),
        ),
        formats = formats,
      ),
    ).forEach { assertEquals(cameraDataVideoStabilizationHighCinematic, it) }

    // Frame rate + StabilizationOff
    listOf(
      getBestFormatByOrder(
        configs = listOf(
          FrameRateConfig(144),
          ResolutionConfig(3000, 2000),
          AspectRatioConfig(aspectRatio = 16F / 9F),
        ),
        formats = formats,
      ),
      getBestFormatByOrder(
        configs = listOf(
          FrameRateConfig(120),
          ResolutionConfig(3000, 2000),
          AspectRatioConfig(aspectRatio = 4F / 3F),
        ),
        formats = formats,
      ),
      getBestFormatByOrder(
        configs = listOf(
          FrameRateConfig(60),
          ResolutionConfig(3000, 2000),
          AspectRatioConfig(aspectRatio = 16F / 9F),
        ),
        formats = formats,
      ),
      getBestFormatByOrder(
        configs = listOf(
          FrameRateConfig(60),
          AspectRatioConfig(aspectRatio = 16F / 9F),
        ),
        formats = formats,
      ),
    ).forEach { assertEquals(cameraDataHighResolution, it) }
  }

  @Test
  fun selectBestFormatByOrder_invokes_onFormatChanged() {
    var inFormatChangedCalled = false
    val formats = cameraDataStandardList

    CameraFormatPicker.selectBestFormatByOrder(
      configs = emptyList(),
      formats = formats,
      onFormatChanged = {
        inFormatChangedCalled = true
        assertEquals(cameraDataHighResolution, it)
      },
      onFrameRateChanged = { },
      onStabilizationModeChanged = { },
    )

    assertEquals(true, inFormatChangedCalled)
  }

  @Test
  fun selectBestFormatByOrder_no_callbacks_invoked_if_formats_empty() {
    CameraFormatPicker.selectBestFormatByOrder(
      configs = emptyList(),
      formats = emptyList(),
      onFormatChanged = { error("OnFormatChanged should not be invoked") },
      onFrameRateChanged = { },
      onStabilizationModeChanged = { },
    )
  }

  @Test
  fun selectBestFormatByOrder_FPS_selection_within_range() {
    var capturedFps: Int? = null
    val target60Fps = 60
    val formats = cameraDataStandardList

    CameraFormatPicker.selectBestFormatByOrder(
      configs = listOf(FrameRateConfig(target60Fps)),
      formats = formats,
      onFormatChanged = { },
      onFrameRateChanged = { capturedFps = it },
      onStabilizationModeChanged = { },
    )

    assertEquals(target60Fps, capturedFps)

    val target35Fps = 35
    CameraFormatPicker.selectBestFormatByOrder(
      configs = listOf(FrameRateConfig(target35Fps)),
      formats = formats,
      onFormatChanged = { },
      onFrameRateChanged = { capturedFps = it },
      onStabilizationModeChanged = { },
    )

    assertEquals(target35Fps, capturedFps)
  }

  @Test
  fun selectBestFormatByOrder_FPS_clamping_to_min() {
    var capturedFps: Int? = null
    val targetLowFps = 1
    val formats = cameraDataStandardList

    CameraFormatPicker.selectBestFormatByOrder(
      configs = listOf(FrameRateConfig(targetLowFps)),
      formats = formats,
      onFormatChanged = { },
      onFrameRateChanged = { capturedFps = it },
      onStabilizationModeChanged = { },
    )

    val expectedMinFps = cameraDataHighResolution.minFps

    assertNotEquals(targetLowFps, capturedFps)
    assertEquals(expectedMinFps, capturedFps)
  }

  @Test
  fun selectBestFormatByOrder_FPS_clamping_to_max() {
    var capturedFps: Int? = null
    val targetHighFps = 999
    val formats = cameraDataStandardList

    CameraFormatPicker.selectBestFormatByOrder(
      configs = listOf(FrameRateConfig(targetHighFps)),
      formats = formats,
      onFormatChanged = { },
      onFrameRateChanged = { capturedFps = it },
      onStabilizationModeChanged = { },
    )

    val expectedMinFps = cameraDataHighResolution.maxFps

    assertNotEquals(targetHighFps, capturedFps)
    assertEquals(expectedMinFps, capturedFps)
  }

  @Test
  fun selectBestFormatByOrder_FPS_default_to_max_when_no_config() {
    var capturedFps: Int? = null
    val formats = cameraDataStandardList

    CameraFormatPicker.selectBestFormatByOrder(
      configs = emptyList(), // No FrameRateConfig
      formats = formats,
      onFormatChanged = { },
      onFrameRateChanged = { capturedFps = it },
      onStabilizationModeChanged = { },
    )

    val expectedMaxFps = cameraDataHighResolution.maxFps
    assertEquals(expectedMaxFps, capturedFps)
  }

  @Test
  fun selectBestFormatByOrder_Stabilization_selection_supported_mode() {
    var capturedMode: VideoStabilizationMode? = null
    val targetMode = VideoStabilizationMode.CinematicExtendedEnhanced
    val formats = cameraDataStabilizationList

    CameraFormatPicker.selectBestFormatByOrder(
      configs = listOf(VideoStabilizationConfig(targetMode)),
      formats = formats,
      onFormatChanged = { },
      onFrameRateChanged = { },
      onStabilizationModeChanged = { capturedMode = it },
    )

    assertEquals(targetMode, capturedMode)
  }

  @Test
  fun selectBestFormatByOrder_Stabilization_fallback_to_Off() {
    var capturedMode: VideoStabilizationMode? = null
    val targetMode = VideoStabilizationMode.CinematicExtendedEnhanced
    val formats = cameraDataStandardList // Does not support cinematic

    CameraFormatPicker.selectBestFormatByOrder(
      configs = listOf(VideoStabilizationConfig(targetMode)),
      formats = formats,
      onFormatChanged = { },
      onFrameRateChanged = { },
      onStabilizationModeChanged = { capturedMode = it },
    )

    assertEquals(VideoStabilizationMode.Off, capturedMode)
  }

  @Test
  fun selectBestFormatByOrder_Stabilization_default_when_no_config() {
    var capturedMode: VideoStabilizationMode? = null
    val formats = cameraDataStabilizationList

    CameraFormatPicker.selectBestFormatByOrder(
      configs = emptyList(), // No VideoStabilizationConfig
      formats = formats,
      onFormatChanged = { },
      onFrameRateChanged = { },
      onStabilizationModeChanged = { capturedMode = it },
    )

    assertEquals(VideoStabilizationMode.Off, capturedMode)
  }

  @Test
  fun selectBestFormatByOrder_catch_exceptions_in_FPS_callback() {
    var formatChangedCalled = false
    var frameRateChangedCalled = false
    CameraFormatPicker.selectBestFormatByOrder(
      configs = listOf(FrameRateConfig(30)),
      formats = cameraDataStandardList,
      onFormatChanged = { formatChangedCalled = true },
      onFrameRateChanged = {
        frameRateChangedCalled = true
        error("Test!")
      },
      onStabilizationModeChanged = { },
    )

    assertEquals(true, frameRateChangedCalled)
    assertEquals(true, formatChangedCalled)
  }

  @Test
  fun selectBestFormatByOrder_catch_exceptions_in_Stabilization_callback() {
    var formatChangedCalled = false
    var stabilizationChangedCalled = false
    CameraFormatPicker.selectBestFormatByOrder(
      configs = listOf(VideoStabilizationConfig(VideoStabilizationMode.Off)),
      formats = cameraDataStandardList,
      onFormatChanged = { formatChangedCalled = true },
      onFrameRateChanged = { },
      onStabilizationModeChanged = {
        stabilizationChangedCalled = true
        error("Test!")
      },
    )

    assertEquals(true, stabilizationChangedCalled)
    assertEquals(true, formatChangedCalled)
  }

  @Test
  fun getBestFormatByOrder_edge_case_zero_resolution_config() {
    val result = getBestFormatByOrder(
      configs = listOf(ResolutionConfig(0, 0)),
      formats = cameraDataStandardList,
    )

    // The smallest resolution due to be closest to (0,0)
    assertEquals(cameraDataLowResolution, result)
  }

  @Test
  fun getBestFormatByOrder_edge_case_null_stabilization_modes_in_format() {
    val formatWithNullStabilization = CameraData(
      width = 1920,
      height = 1080,
      videoStabilizationModes = null,
    )

    val result = getBestFormatByOrder(
      configs = listOf(VideoStabilizationConfig(VideoStabilizationMode.CinematicExtendedEnhanced)),
      formats = listOf(formatWithNullStabilization),
    )

    assertNotNull(result)
    assertEquals(formatWithNullStabilization, result)
  }

  @Test
  fun getBestFormatByOrder_partial_match_scoring() {
    val formatExactMatch = CameraData(
      width = 1920,
      height = 1080,
      videoStabilizationModes = listOf(VideoStabilizationMode.Cinematic),
    )
    val formatPartialMatch = CameraData(
      width = 1920,
      height = 1080,
      videoStabilizationModes = listOf(VideoStabilizationMode.Standard),
    )
    val formatNoModes = CameraData(
      width = 1920,
      height = 1080,
      videoStabilizationModes = emptyList(),
    )

    val config = listOf(VideoStabilizationConfig(VideoStabilizationMode.Cinematic))

    // Format exact, partial & no modes
    val resultAll = getBestFormatByOrder(
      configs = config,
      formats = listOf(formatNoModes, formatPartialMatch, formatExactMatch),
    )
    assertEquals(formatExactMatch, resultAll)

    // Format partial & no modes
    val resultPartialVsNone = getBestFormatByOrder(
      configs = config,
      formats = listOf(formatNoModes, formatPartialMatch),
    )
    assertEquals(formatPartialMatch, resultPartialVsNone)

    // Format no modes
    val resultNone = getBestFormatByOrder(configs = config, formats = listOf(formatNoModes))
    assertEquals(formatNoModes, resultNone)
  }
}
