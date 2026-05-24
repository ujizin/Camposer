package com.ujizin.camposer.state.properties

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class CameraDataTest {
  // ── equals ───────────────────────────────────────────────────────────────────

  @Test
  fun test_equals_same_instance() {
    val data = CameraData(1920, 1080)
    assertEquals(data, data)
  }

  @Test
  fun test_equals_identical_fields() {
    assertEquals(CameraData(1920, 1080), CameraData(1920, 1080))
  }

  @Test
  fun test_not_equal_different_width() {
    assertNotEquals(CameraData(1920, 1080), CameraData(1280, 1080))
  }

  @Test
  fun test_not_equal_different_height() {
    assertNotEquals(CameraData(1920, 1080), CameraData(1920, 720))
  }

  @Test
  fun test_not_equal_different_min_fps() {
    assertNotEquals(CameraData(1920, 1080, minFps = 24), CameraData(1920, 1080, minFps = 30))
  }

  @Test
  fun test_not_equal_different_max_fps() {
    assertNotEquals(CameraData(1920, 1080, maxFps = 30), CameraData(1920, 1080, maxFps = 60))
  }

  @Test
  fun test_not_equal_different_focus_support() {
    assertNotEquals(
      CameraData(1920, 1080, isFocusSupported = true),
      CameraData(1920, 1080, isFocusSupported = false),
    )
  }

  @Test
  fun test_not_equal_different_stabilization_modes() {
    assertNotEquals(
      CameraData(1920, 1080, videoStabilizationModes = listOf(VideoStabilizationMode.Standard)),
      CameraData(1920, 1080, videoStabilizationModes = null),
    )
  }

  @Test
  fun test_not_equal_to_null() {
    assertFalse(CameraData(1920, 1080).equals(null))
  }

  @Test
  fun test_not_equal_to_different_type() {
    assertFalse(CameraData(1920, 1080).equals("string"))
  }

  // ── hashCode ─────────────────────────────────────────────────────────────────

  @Test
  fun test_hashcode_equal_for_equal_instances() {
    assertEquals(CameraData(1920, 1080).hashCode(), CameraData(1920, 1080).hashCode())
  }

  @Test
  fun test_hashcode_differs_for_different_width() {
    assertNotEquals(CameraData(1920, 1080).hashCode(), CameraData(1280, 1080).hashCode())
  }

  // ── toString ─────────────────────────────────────────────────────────────────

  @Test
  fun test_tostring_is_not_empty() {
    assertTrue(CameraData(1920, 1080).toString().isNotEmpty())
  }

  @Test
  fun test_tostring_contains_width_and_height() {
    val str = CameraData(1920, 1080).toString()
    assertTrue(str.contains("1920"), "toString='$str'")
    assertTrue(str.contains("1080"), "toString='$str'")
  }
}
