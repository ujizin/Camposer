package com.ujizin.camposer.manager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class CameraDeviceStateTest {
  @Test
  fun test_initial_equality() {
    assertEquals(CameraDeviceState.Initial, CameraDeviceState.Initial)
  }

  @Test
  fun test_devices_state_with_empty_list() {
    val state = CameraDeviceState.Devices(emptyList())
    assertTrue(state.cameraDevices.isEmpty())
  }

  @Test
  fun test_devices_equality_same_list() {
    val a = CameraDeviceState.Devices(emptyList())
    val b = CameraDeviceState.Devices(emptyList())
    assertEquals(a, b)
  }

  @Test
  fun test_initial_not_equal_to_devices() {
    val initial: CameraDeviceState = CameraDeviceState.Initial
    val devices: CameraDeviceState = CameraDeviceState.Devices(emptyList())
    assertNotEquals(initial, devices)
  }

  @Test
  fun test_devices_instance_equal_to_itself() {
    val state = CameraDeviceState.Devices(emptyList())
    assertEquals(state, state)
  }

  @Test
  fun test_initial_is_not_devices() {
    val state: CameraDeviceState = CameraDeviceState.Initial
    assertFalse(state is CameraDeviceState.Devices)
  }
}
