package com.ujizin.camposer.format

import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.format.Default
import com.ujizin.camposer.state.properties.format.High
import com.ujizin.camposer.state.properties.format.Low
import com.ujizin.camposer.state.properties.format.Medium
import com.ujizin.camposer.state.properties.format.UltraHigh
import com.ujizin.camposer.state.properties.format.config.AspectRatioConfig
import com.ujizin.camposer.state.properties.format.config.FrameRateConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import com.ujizin.camposer.state.properties.format.config.VideoStabilizationConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CamFormatTest {
  // region CamFormat class

  @Test
  fun test_no_arg_constructor_uses_high_resolution_config() {
    assertEquals(listOf(ResolutionConfig.High), CamFormat().configs)
  }

  @Test
  fun test_vararg_stores_configs_in_order() {
    val resolution = ResolutionConfig(1920, 1080)
    val frameRate = FrameRateConfig(30)
    val format = CamFormat(resolution, frameRate)
    assertEquals(listOf(resolution, frameRate), format.configs)
  }

  @Test
  fun test_equals_same_configs() {
    val a = CamFormat(ResolutionConfig(1920, 1080))
    val b = CamFormat(ResolutionConfig(1920, 1080))
    assertEquals(a, b)
  }

  @Test
  fun test_not_equals_different_configs() {
    val a = CamFormat(ResolutionConfig(1920, 1080))
    val b = CamFormat(ResolutionConfig(1280, 720))
    assertNotEquals(a, b)
  }

  @Test
  fun test_not_equals_different_config_count() {
    val a = CamFormat(ResolutionConfig(1920, 1080))
    val b = CamFormat(ResolutionConfig(1920, 1080), FrameRateConfig(30))
    assertNotEquals(a, b)
  }

  @Test
  fun test_not_equals_different_config_order() {
    val resolution = ResolutionConfig(1920, 1080)
    val frameRate = FrameRateConfig(30)
    val a = CamFormat(resolution, frameRate)
    val b = CamFormat(frameRate, resolution)
    assertNotEquals(a, b)
  }

  @Test
  fun test_hashCode_equal_for_equal_instances() {
    val a = CamFormat(ResolutionConfig(1920, 1080))
    val b = CamFormat(ResolutionConfig(1920, 1080))
    assertEquals(a.hashCode(), b.hashCode())
  }

  @Test
  fun test_toString_is_not_empty() {
    assertTrue(CamFormat(ResolutionConfig(1920, 1080)).toString().isNotEmpty())
  }

  // endregion

  // region CamFormat companion extensions (CamFormatKt)

  @Test
  fun test_ultra_high_has_ultra_high_resolution() {
    assertEquals(listOf(ResolutionConfig.UltraHigh), CamFormat.UltraHigh.configs)
  }

  @Test
  fun test_ultra_high_resolution_is_3840x2160() {
    val config = CamFormat.UltraHigh.configs.first() as ResolutionConfig
    assertEquals(3840, config.width)
    assertEquals(2160, config.height)
  }

  @Test
  fun test_high_has_high_resolution() {
    assertEquals(listOf(ResolutionConfig.High), CamFormat.High.configs)
  }

  @Test
  fun test_high_resolution_is_1920x1080() {
    val config = CamFormat.High.configs.first() as ResolutionConfig
    assertEquals(1920, config.width)
    assertEquals(1080, config.height)
  }

  @Test
  fun test_medium_has_medium_resolution() {
    assertEquals(listOf(ResolutionConfig.Medium), CamFormat.Medium.configs)
  }

  @Test
  fun test_medium_resolution_is_1280x720() {
    val config = CamFormat.Medium.configs.first() as ResolutionConfig
    assertEquals(1280, config.width)
    assertEquals(720, config.height)
  }

  @Test
  fun test_low_has_low_resolution() {
    assertEquals(listOf(ResolutionConfig.Low), CamFormat.Low.configs)
  }

  @Test
  fun test_low_resolution_is_720x480() {
    val config = CamFormat.Low.configs.first() as ResolutionConfig
    assertEquals(720, config.width)
    assertEquals(480, config.height)
  }

  @Test
  fun test_default_equals_high() {
    assertEquals(CamFormat.High, CamFormat.Default)
  }

  @Test
  fun test_no_arg_equals_default() {
    assertEquals(CamFormat.Default, CamFormat())
  }

  @Test
  fun test_resolutions_ordered_by_size() {
    fun CamFormat.megapixels(): Long {
      val res = configs.first() as ResolutionConfig
      return res.width.toLong() * res.height
    }
    assertTrue(CamFormat.UltraHigh.megapixels() > CamFormat.High.megapixels())
    assertTrue(CamFormat.High.megapixels() > CamFormat.Medium.megapixels())
    assertTrue(CamFormat.Medium.megapixels() > CamFormat.Low.megapixels())
  }

  // endregion

  // region CameraData.toCameraFormat()

  @Test
  fun test_toCameraFormat_includes_resolution_config() {
    val data = CameraData(width = 1920, height = 1080)
    val format = data.toCameraFormat()
    assertTrue(
      format.configs.any { it is ResolutionConfig && it.width == 1920 && it.height == 1080 },
    )
  }

  @Test
  fun test_toCameraFormat_includes_aspect_ratio_config() {
    val data = CameraData(width = 1920, height = 1080)
    val format = data.toCameraFormat()
    assertTrue(format.configs.any { it is AspectRatioConfig })
  }

  @Test
  fun test_toCameraFormat_aspect_ratio_is_width_over_height() {
    val data = CameraData(width = 1920, height = 1080)
    val format = data.toCameraFormat()
    val aspectConfig = format.configs.filterIsInstance<AspectRatioConfig>().first()
    assertEquals(1920f / 1080f, aspectConfig.aspectRatio)
  }

  @Test
  fun test_toCameraFormat_includes_frame_rate_config_with_max_fps() {
    val data = CameraData(width = 1920, height = 1080, minFps = 8, maxFps = 60)
    val format = data.toCameraFormat()
    val fpsConfig = format.configs.filterIsInstance<FrameRateConfig>().firstOrNull()
    assertEquals(60, fpsConfig?.fps)
  }

  @Test
  fun test_toCameraFormat_no_frame_rate_config_when_fps_null() {
    val data = CameraData(width = 1920, height = 1080)
    val format = data.toCameraFormat()
    assertFalse(format.configs.any { it is FrameRateConfig })
  }

  @Test
  fun test_toCameraFormat_includes_stabilization_config_with_last_mode() {
    val data =
      CameraData(
        width = 1920,
        height = 1080,
        videoStabilizationModes = listOf(
          VideoStabilizationMode.Standard,
          VideoStabilizationMode.Cinematic,
        ),
      )
    val format = data.toCameraFormat()
    val stabilConfig = format.configs.filterIsInstance<VideoStabilizationConfig>().firstOrNull()
    assertEquals(VideoStabilizationMode.Cinematic, stabilConfig?.mode)
  }

  @Test
  fun test_toCameraFormat_no_stabilization_config_when_modes_null() {
    val data = CameraData(width = 1920, height = 1080, videoStabilizationModes = null)
    val format = data.toCameraFormat()
    assertFalse(format.configs.any { it is VideoStabilizationConfig })
  }

  @Test
  fun test_toCameraFormat_no_stabilization_config_when_modes_empty() {
    val data = CameraData(width = 1920, height = 1080, videoStabilizationModes = emptyList())
    val format = data.toCameraFormat()
    assertFalse(format.configs.any { it is VideoStabilizationConfig })
  }

  // endregion

  // region CameraData.getStabilizationModeByConfigs()

  @Test
  fun test_getStabilizationModeByConfigs_no_config_returns_off() {
    val data =
      CameraData(
        width = 1920,
        height = 1080,
        videoStabilizationModes = listOf(VideoStabilizationMode.Cinematic),
      )
    assertEquals(VideoStabilizationMode.Off, data.getStabilizationModeByConfigs(emptyList()))
  }

  @Test
  fun test_getStabilizationModeByConfigs_supported_mode_returns_that_mode() {
    val data =
      CameraData(
        width = 1920,
        height = 1080,
        videoStabilizationModes = listOf(VideoStabilizationMode.Cinematic),
      )
    assertEquals(
      VideoStabilizationMode.Cinematic,
      data.getStabilizationModeByConfigs(
        listOf(VideoStabilizationConfig(VideoStabilizationMode.Cinematic)),
      ),
    )
  }

  @Test
  fun test_getStabilizationModeByConfigs_unsupported_mode_returns_off() {
    val data =
      CameraData(
        width = 1920,
        height = 1080,
        videoStabilizationModes = listOf(VideoStabilizationMode.Standard),
      )
    assertEquals(
      VideoStabilizationMode.Off,
      data.getStabilizationModeByConfigs(
        listOf(VideoStabilizationConfig(VideoStabilizationMode.Cinematic)),
      ),
    )
  }

  @Test
  fun test_getStabilizationModeByConfigs_null_modes_returns_off() {
    val data = CameraData(width = 1920, height = 1080, videoStabilizationModes = null)
    assertEquals(
      VideoStabilizationMode.Off,
      data.getStabilizationModeByConfigs(
        listOf(VideoStabilizationConfig(VideoStabilizationMode.Cinematic)),
      ),
    )
  }

  @Test
  fun test_getStabilizationModeByConfigs_non_stabilization_config_returns_off() {
    val data =
      CameraData(
        width = 1920,
        height = 1080,
        videoStabilizationModes = listOf(VideoStabilizationMode.Cinematic),
      )
    assertEquals(
      VideoStabilizationMode.Off,
      data.getStabilizationModeByConfigs(listOf(ResolutionConfig(1920, 1080))),
    )
  }

  // endregion

  // region CameraData.getFrameRateByConfigs()

  @Test
  fun test_getFrameRateByConfigs_no_config_returns_maxFps() {
    val data = CameraData(width = 1920, height = 1080, minFps = 8, maxFps = 60)
    assertEquals(60, data.getFrameRateByConfigs(emptyList()))
  }

  @Test
  fun test_getFrameRateByConfigs_fps_in_range_returned_as_is() {
    val data = CameraData(width = 1920, height = 1080, minFps = 8, maxFps = 60)
    assertEquals(30, data.getFrameRateByConfigs(listOf(FrameRateConfig(30))))
  }

  @Test
  fun test_getFrameRateByConfigs_fps_at_min_boundary_returned() {
    val data = CameraData(width = 1920, height = 1080, minFps = 8, maxFps = 60)
    assertEquals(8, data.getFrameRateByConfigs(listOf(FrameRateConfig(8))))
  }

  @Test
  fun test_getFrameRateByConfigs_fps_at_max_boundary_returned() {
    val data = CameraData(width = 1920, height = 1080, minFps = 8, maxFps = 60)
    assertEquals(60, data.getFrameRateByConfigs(listOf(FrameRateConfig(60))))
  }

  @Test
  fun test_getFrameRateByConfigs_fps_below_min_clamped_to_min() {
    val data = CameraData(width = 1920, height = 1080, minFps = 8, maxFps = 60)
    assertEquals(8, data.getFrameRateByConfigs(listOf(FrameRateConfig(1))))
  }

  @Test
  fun test_getFrameRateByConfigs_fps_above_max_clamped_to_max() {
    val data = CameraData(width = 1920, height = 1080, minFps = 8, maxFps = 60)
    assertEquals(60, data.getFrameRateByConfigs(listOf(FrameRateConfig(999))))
  }

  @Test
  fun test_getFrameRateByConfigs_null_fps_returns_negative_one() {
    val data = CameraData(width = 1920, height = 1080)
    assertEquals(-1, data.getFrameRateByConfigs(emptyList()))
  }

  @Test
  fun test_getFrameRateByConfigs_non_fps_config_returns_maxFps() {
    val data = CameraData(width = 1920, height = 1080, minFps = 8, maxFps = 60)
    assertEquals(60, data.getFrameRateByConfigs(listOf(ResolutionConfig(1920, 1080))))
  }

  // endregion
}
