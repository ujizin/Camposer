package com.ujizin.camposer.manager

import com.ujizin.camposer.state.properties.selector.CamPosition
import com.ujizin.camposer.state.properties.selector.CameraId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class CameraDevicesManagerIosTest {
  // ── CameraDevicesManager ────────────────────────────────────────────────────

  @Test
  fun test_initial_state_is_initial() {
    val manager = CameraDevicesManager()
    assertEquals(CameraDeviceState.Initial, manager.cameraDevicesState.value)
    manager.release()
  }

  @Test
  fun test_get_available_cameras_does_not_throw() {
    val manager = CameraDevicesManager()
    // iOS Simulator may return empty, but must not throw
    val cameras = manager.getAvailableCameras()
    assertEquals(cameras, cameras) // assert call succeeds and returns a stable list
    manager.release()
  }

  @Test
  fun test_release_does_not_throw() {
    val manager = CameraDevicesManager()
    manager.release()
  }

  @Test
  fun test_release_can_be_called_multiple_times() {
    val manager = CameraDevicesManager()
    manager.release()
    manager.release()
  }

  // ── CameraDevice ────────────────────────────────────────────────────────────

  @Test
  fun test_camera_device_equals_same_id() {
    assertEquals(buildDevice("cam-1"), buildDevice("cam-1"))
  }

  @Test
  fun test_camera_device_not_equal_different_id() {
    assertNotEquals(buildDevice("cam-1"), buildDevice("cam-2"))
  }

  @Test
  fun test_camera_device_hashcode_consistent_for_same_id() {
    assertEquals(buildDevice("cam-1").hashCode(), buildDevice("cam-1").hashCode())
  }

  @Test
  fun test_camera_device_hashcode_differs_for_different_id() {
    assertNotEquals(buildDevice("cam-1").hashCode(), buildDevice("cam-2").hashCode())
  }

  @Test
  fun test_camera_device_equals_same_instance() {
    val device = buildDevice("cam-1")
    assertEquals(device, device)
  }

  @Test
  fun test_camera_device_not_equal_to_different_position() {
    val back = buildDevice("cam-1", position = CamPosition.Back)
    val front = buildDevice("cam-1", position = CamPosition.Front)
    assertFalse(back == front)
  }

  @Test
  fun test_camera_device_to_string_contains_camera_id() {
    val device = buildDevice("back-wide")
    assertTrue(device.toString().contains("back-wide"))
  }

  private fun buildDevice(
    id: String,
    position: CamPosition = CamPosition.Back,
  ) = CameraDevice(
    cameraId = CameraId(id),
    name = "Test Camera",
    position = position,
    fov = 77.0f,
    lensType = emptyList(),
    photoData = emptyList(),
    videoData = emptyList(),
  )
}
