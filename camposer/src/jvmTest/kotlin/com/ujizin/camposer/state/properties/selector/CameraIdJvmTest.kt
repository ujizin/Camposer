package com.ujizin.camposer.state.properties.selector

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class CameraIdJvmTest {
  @Test
  fun test_equality_by_index() {
    assertEquals(CameraId(1), CameraId(1))
    assertEquals(CameraId(1).hashCode(), CameraId(1).hashCode())
    assertNotEquals(CameraId(1), CameraId(2))
  }

  @Test
  fun test_to_string() {
    assertEquals("CameraId(index=4)", CameraId(4).toString())
  }
}
