package com.ujizin.camposer.state.properties.format

import com.ujizin.camposer.fake.FakeJvmCameraController
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.config.AspectRatioConfig
import com.ujizin.camposer.state.properties.format.config.FrameRateConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import com.ujizin.camposer.state.properties.format.config.VideoStabilizationConfig
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CamFormatJvmTest {
  private val controller = FakeJvmCameraController()
  private var frameRate: Int? = null
  private var stabilization: VideoStabilizationMode? = null

  private fun CamFormat.apply() =
    applyConfigs(
      jvmCameraController = controller,
      onFrameRateChanged = { frameRate = it },
      onStabilizationModeChanged = { stabilization = it },
    )

  @Test
  fun test_resolution_config_is_applied() {
    CamFormat(ResolutionConfig(1280, 720)).apply()

    assertEquals(1280, controller.currentFormat.width)
    assertEquals(720, controller.currentFormat.height)
  }

  @Test
  fun test_resolution_snaps_to_nearest_candidate() {
    CamFormat(ResolutionConfig(3860, 2080)).apply()

    assertEquals(3840, controller.currentFormat.width)
    assertEquals(2160, controller.currentFormat.height)
  }

  @Test
  fun test_aspect_ratio_config_picks_matching_candidate() {
    CamFormat(AspectRatioConfig(4F / 3F)).apply()

    val ratio = controller.currentFormat.width.toFloat() / controller.currentFormat.height
    assertEquals(4F / 3F, ratio)
  }

  @Test
  fun test_frame_rate_is_only_changed_when_requested() {
    CamFormat(ResolutionConfig.High).apply()
    assertEquals(null, frameRate)

    CamFormat(ResolutionConfig.High, FrameRateConfig(60)).apply()
    assertEquals(60, frameRate)
  }

  @Test
  fun test_stabilization_is_always_off() {
    CamFormat(VideoStabilizationConfig(VideoStabilizationMode.Cinematic)).apply()

    assertEquals(VideoStabilizationMode.Off, stabilization)
  }

  @Test
  fun test_default_constructor_matches_default_format() {
    assertEquals(CamFormat.Default, CamFormat())
    assertEquals(CamFormat.Default.hashCode(), CamFormat().hashCode())
    assertEquals("CamFormat(configs=[Resolution(width=1920, height=1080)])", CamFormat().toString())
  }
}
