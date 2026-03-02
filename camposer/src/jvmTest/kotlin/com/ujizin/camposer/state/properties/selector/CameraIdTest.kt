package com.ujizin.camposer.state.properties.selector

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class CameraIdTest {
  @Test
  fun `equality is based on deviceId`() {
    val id1 = CameraId("0")
    val id2 = CameraId("0")
    val id3 = CameraId("1")
    assertEquals(id1, id2)
    assertNotEquals(id1, id3)
  }

  @Test
  fun `toString includes deviceId`() {
    val id = CameraId("42")
    assertEquals("CameraId(deviceId=42)", id.toString())
  }

  @Test
  fun `equal instances have equal hashCodes`() {
    val id1 = CameraId("0")
    val id2 = CameraId("0")
    assertEquals(id1.hashCode(), id2.hashCode())
  }
}
