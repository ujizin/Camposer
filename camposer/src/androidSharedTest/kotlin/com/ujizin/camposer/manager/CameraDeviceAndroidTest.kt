package com.ujizin.camposer.manager

import android.annotation.SuppressLint
import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.selector.CamLensType
import com.ujizin.camposer.state.properties.selector.CamPosition
import com.ujizin.camposer.state.properties.selector.CameraId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class CameraDeviceAndroidTest {
  // ── equals ───────────────────────────────────────────────────────────────────

  @Test
  fun test_equals_same_instance() {
    val device = buildDevice()
    assertEquals(device, device)
  }

  @Test
  fun test_equals_identical_fields() {
    assertEquals(buildDevice(), buildDevice())
  }

  @Test
  fun test_not_equal_different_position() {
    assertNotEquals(
      buildDevice(position = CamPosition.Back),
      buildDevice(position = CamPosition.Front),
    )
  }

  @Test
  fun test_not_equal_different_name() {
    assertNotEquals(
      buildDevice(name = "Camera A"),
      buildDevice(name = "Camera B"),
    )
  }

  @Test
  fun test_not_equal_different_fov() {
    assertNotEquals(
      buildDevice(fov = 60f),
      buildDevice(fov = 90f),
    )
  }

  @Test
  fun test_not_equal_different_lens_types() {
    assertNotEquals(
      buildDevice(lensType = listOf(CamLensType.Wide)),
      buildDevice(lensType = listOf(CamLensType.Telephoto)),
    )
  }

  @Test
  fun test_not_equal_different_photo_data() {
    assertNotEquals(
      buildDevice(photoData = listOf(CameraData(1920, 1080))),
      buildDevice(photoData = emptyList()),
    )
  }

  @Test
  fun test_not_equal_different_video_data() {
    assertNotEquals(
      buildDevice(videoData = listOf(CameraData(1280, 720))),
      buildDevice(videoData = emptyList()),
    )
  }

  @Test
  fun test_not_equal_to_null() {
    assertFalse(buildDevice().equals(null))
  }

  @Test
  fun test_not_equal_to_different_type() {
    assertFalse(buildDevice().equals("not a device"))
  }

  // ── hashCode ─────────────────────────────────────────────────────────────────

  @Test
  fun test_hashcode_equal_for_equal_instances() {
    assertEquals(buildDevice().hashCode(), buildDevice().hashCode())
  }

  @Test
  fun test_hashcode_differs_for_different_positions() {
    assertNotEquals(
      buildDevice(position = CamPosition.Back).hashCode(),
      buildDevice(position = CamPosition.Front).hashCode(),
    )
  }

  // ── toString ─────────────────────────────────────────────────────────────────

  @Test
  fun test_tostring_is_not_empty() {
    assertTrue(buildDevice().toString().isNotEmpty())
  }

  @Test
  fun test_tostring_contains_camera_id() {
    val device = buildDevice(ids = listOf("camera-42"))
    assertTrue(
      device.toString().contains("42"),
      "toString='$device'",
    )
  }

  // ── helpers ──────────────────────────────────────────────────────────────────

  @SuppressLint("RestrictedApi")
  private fun buildDevice(
    ids: List<String> = listOf("0"),
    name: String = "Test Camera",
    position: CamPosition = CamPosition.Back,
    fov: Float = 77f,
    lensType: List<CamLensType> = listOf(CamLensType.Wide),
    photoData: List<CameraData> = emptyList(),
    videoData: List<CameraData> = emptyList(),
  ) = CameraDevice(
    cameraId = CameraId(identifier = null, physicalCameraInfos = emptySet(), ids = ids),
    name = name,
    position = position,
    fov = fov,
    lensType = lensType,
    photoData = photoData,
    videoData = videoData,
  )
}
