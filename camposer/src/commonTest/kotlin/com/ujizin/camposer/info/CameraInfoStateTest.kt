package com.ujizin.camposer.info

import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class CameraInfoStateTest {
  // ── equals ───────────────────────────────────────────────────────────────────

  @Test
  fun test_equals_same_instance() {
    val state = CameraInfoState()
    assertEquals(state, state)
  }

  @Test
  fun test_equals_two_default_instances() {
    assertEquals(CameraInfoState(), CameraInfoState())
  }

  @Test
  fun test_equals_matching_non_default_fields() {
    val a = CameraInfoState(isFlashSupported = true, minZoom = 1f, maxZoom = 8f)
    val b = CameraInfoState(isFlashSupported = true, minZoom = 1f, maxZoom = 8f)
    assertEquals(a, b)
  }

  @Test
  fun test_not_equal_different_is_zoom_supported() {
    assertNotEquals(
      CameraInfoState(isZoomSupported = true),
      CameraInfoState(isZoomSupported = false),
    )
  }

  @Test
  fun test_not_equal_different_is_flash_supported() {
    assertNotEquals(
      CameraInfoState(isFlashSupported = true),
      CameraInfoState(isFlashSupported = false),
    )
  }

  @Test
  fun test_not_equal_different_min_zoom() {
    assertNotEquals(CameraInfoState(minZoom = 1f), CameraInfoState(minZoom = 2f))
  }

  @Test
  fun test_not_equal_different_max_zoom() {
    assertNotEquals(CameraInfoState(maxZoom = 5f), CameraInfoState(maxZoom = 10f))
  }

  @Test
  fun test_not_equal_different_min_fps() {
    assertNotEquals(CameraInfoState(minFPS = 24), CameraInfoState(minFPS = 30))
  }

  @Test
  fun test_not_equal_different_max_fps() {
    assertNotEquals(CameraInfoState(maxFPS = 30), CameraInfoState(maxFPS = 60))
  }

  @Test
  fun test_not_equal_different_photo_formats() {
    val a = CameraInfoState(photoFormats = listOf(CameraData(1920, 1080)))
    val b = CameraInfoState(photoFormats = emptyList())
    assertNotEquals(a, b)
  }

  @Test
  fun test_not_equal_different_video_formats() {
    val a = CameraInfoState(videoFormats = listOf(CameraData(1280, 720)))
    val b = CameraInfoState(videoFormats = emptyList())
    assertNotEquals(a, b)
  }

  @Test
  fun test_not_equal_to_null() {
    val state = CameraInfoState()
    assertFalse(state.equals(null))
  }

  @Test
  fun test_not_equal_to_different_type() {
    val state = CameraInfoState()
    assertFalse(state.equals("string"))
  }

  // ── hashCode ─────────────────────────────────────────────────────────────────

  @Test
  fun test_hashcode_equal_for_equal_instances() {
    val a = CameraInfoState(isFlashSupported = true, maxZoom = 5f)
    val b = CameraInfoState(isFlashSupported = true, maxZoom = 5f)
    assertEquals(a.hashCode(), b.hashCode())
  }

  @Test
  fun test_hashcode_differs_for_different_instances() {
    val a = CameraInfoState(maxZoom = 5f)
    val b = CameraInfoState(maxZoom = 10f)
    assertNotEquals(a.hashCode(), b.hashCode())
  }

  // ── toString ─────────────────────────────────────────────────────────────────

  @Test
  fun test_tostring_is_not_empty() {
    assertTrue(CameraInfoState().toString().isNotEmpty())
  }

  @Test
  fun test_tostring_contains_min_zoom() {
    val str = CameraInfoState(minZoom = 1.5f).toString()
    assertTrue(str.contains("minZoom"), "toString='$str'")
  }

  @Test
  fun test_tostring_contains_max_zoom() {
    val str = CameraInfoState(maxZoom = 8f).toString()
    assertTrue(str.contains("maxZoom"), "toString='$str'")
  }

  // ── isVideoStabilizationSupported() ─────────────────────────────────────────

  @Test
  fun test_is_video_stabilization_supported_false_for_empty_list() {
    assertFalse(emptyList<CameraData>().isVideoStabilizationSupported())
  }

  @Test
  fun test_is_video_stabilization_supported_false_when_only_off_mode() {
    val data = CameraData(1920, 1080, videoStabilizationModes = listOf(VideoStabilizationMode.Off))
    assertFalse(listOf(data).isVideoStabilizationSupported())
  }

  @Test
  fun test_is_video_stabilization_supported_false_when_no_modes() {
    val data = CameraData(1920, 1080, videoStabilizationModes = null)
    assertFalse(listOf(data).isVideoStabilizationSupported())
  }

  @Test
  fun test_is_video_stabilization_supported_true_when_standard_mode() {
    val data = CameraData(
      1920,
      1080,
      videoStabilizationModes = listOf(VideoStabilizationMode.Standard),
    )
    assertTrue(listOf(data).isVideoStabilizationSupported())
  }

  @Test
  fun test_is_video_stabilization_supported_true_when_cinematic_mode() {
    val data = CameraData(
      1920,
      1080,
      videoStabilizationModes = listOf(VideoStabilizationMode.Cinematic),
    )
    assertTrue(listOf(data).isVideoStabilizationSupported())
  }

  @Test
  fun test_is_video_stabilization_supported_true_when_mixed_modes_include_non_off() {
    val data = CameraData(
      1920,
      1080,
      videoStabilizationModes = listOf(VideoStabilizationMode.Off, VideoStabilizationMode.Standard),
    )
    assertTrue(listOf(data).isVideoStabilizationSupported())
  }
}
