package com.ujizin.camposer.state.properties.format.config

import com.ujizin.camposer.state.properties.VideoStabilizationMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class CameraFormatConfigTest {
  // ── AspectRatioConfig ─────────────────────────────────────────────────────────

  @Test
  fun test_aspect_ratio_config_equals_same_ratio() {
    assertEquals(AspectRatioConfig(1.77f), AspectRatioConfig(1.77f))
  }

  @Test
  fun test_aspect_ratio_config_not_equal_different_ratio() {
    assertNotEquals(AspectRatioConfig(1.77f), AspectRatioConfig(1.33f))
  }

  @Test
  fun test_aspect_ratio_config_not_equal_to_null() {
    assertFalse(AspectRatioConfig(1.77f).equals(null))
  }

  @Test
  fun test_aspect_ratio_config_not_equal_to_different_type() {
    assertFalse(AspectRatioConfig(1.77f).equals("string"))
  }

  @Test
  fun test_aspect_ratio_config_hashcode_equal_for_equal_instances() {
    assertEquals(AspectRatioConfig(1.77f).hashCode(), AspectRatioConfig(1.77f).hashCode())
  }

  @Test
  fun test_aspect_ratio_tostring_1_to_1() {
    val str = AspectRatioConfig(1f).toString()
    assertTrue(str.contains("1:1"), "toString='$str'")
  }

  @Test
  fun test_aspect_ratio_tostring_4_to_3() {
    val str = AspectRatioConfig(4f / 3f).toString()
    assertTrue(str.contains("4:3"), "toString='$str'")
  }

  @Test
  fun test_aspect_ratio_tostring_16_to_9() {
    val str = AspectRatioConfig(16f / 9f).toString()
    assertTrue(str.contains("16:9"), "toString='$str'")
  }

  @Test
  fun test_aspect_ratio_tostring_unknown_ratio_shows_value() {
    val ratio = 2.0f
    val str = AspectRatioConfig(ratio).toString()
    assertTrue(str.contains(ratio.toString()), "toString='$str'")
  }

  // ── VideoStabilizationConfig ──────────────────────────────────────────────────

  @Test
  fun test_video_stabilization_config_equals_same_mode() {
    assertEquals(
      VideoStabilizationConfig(VideoStabilizationMode.Standard),
      VideoStabilizationConfig(VideoStabilizationMode.Standard),
    )
  }

  @Test
  fun test_video_stabilization_config_not_equal_different_mode() {
    assertNotEquals(
      VideoStabilizationConfig(VideoStabilizationMode.Standard),
      VideoStabilizationConfig(VideoStabilizationMode.Cinematic),
    )
  }

  @Test
  fun test_video_stabilization_config_not_equal_to_null() {
    assertFalse(VideoStabilizationConfig().equals(null))
  }

  @Test
  fun test_video_stabilization_config_not_equal_to_different_type() {
    assertFalse(VideoStabilizationConfig().equals("string"))
  }

  @Test
  fun test_video_stabilization_config_hashcode_equal_for_equal_instances() {
    assertEquals(
      VideoStabilizationConfig(VideoStabilizationMode.Standard).hashCode(),
      VideoStabilizationConfig(VideoStabilizationMode.Standard).hashCode(),
    )
  }

  @Test
  fun test_video_stabilization_config_tostring_contains_mode() {
    val str = VideoStabilizationConfig(VideoStabilizationMode.Cinematic).toString()
    assertTrue(str.contains("Cinematic"), "toString='$str'")
  }

  @Test
  fun test_video_stabilization_config_default_is_standard() {
    assertEquals(VideoStabilizationMode.Standard, VideoStabilizationConfig().mode)
  }

  // ── FrameRateConfig ───────────────────────────────────────────────────────────

  @Test
  fun test_frame_rate_config_equals_same_fps() {
    assertEquals(FrameRateConfig(30), FrameRateConfig(30))
  }

  @Test
  fun test_frame_rate_config_not_equal_different_fps() {
    assertNotEquals(FrameRateConfig(30), FrameRateConfig(60))
  }

  @Test
  fun test_frame_rate_config_not_equal_to_null() {
    assertFalse(FrameRateConfig(30).equals(null))
  }

  @Test
  fun test_frame_rate_config_not_equal_to_different_type() {
    assertFalse(FrameRateConfig(30).equals("string"))
  }

  @Test
  fun test_frame_rate_config_tostring_contains_fps() {
    val str = FrameRateConfig(60).toString()
    assertTrue(str.contains("60"), "toString='$str'")
  }
}
