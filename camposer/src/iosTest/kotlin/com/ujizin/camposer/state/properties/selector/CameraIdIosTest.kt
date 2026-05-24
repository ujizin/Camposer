package com.ujizin.camposer.state.properties.selector

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class CameraIdIosTest {
  @Test
  fun test_camera_ids_with_same_unique_id_are_equal() {
    assertEquals(CameraId("device-1"), CameraId("device-1"))
  }

  @Test
  fun test_camera_ids_with_different_unique_ids_are_not_equal() {
    assertNotEquals(CameraId("device-1"), CameraId("device-2"))
  }

  @Test
  fun test_camera_id_hashcode_equal_for_same_unique_id() {
    assertEquals(CameraId("device-1").hashCode(), CameraId("device-1").hashCode())
  }

  @Test
  fun test_camera_id_hashcode_differs_for_different_unique_ids() {
    assertNotEquals(CameraId("device-1").hashCode(), CameraId("device-2").hashCode())
  }

  @Test
  fun test_camera_id_to_string() {
    assertEquals("CameraId(uniqueId=device-1)", CameraId("device-1").toString())
  }
}
