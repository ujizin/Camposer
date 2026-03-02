package com.ujizin.camposer.state.properties.selector

import com.ujizin.camposer.manager.CameraDevice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class CamSelectorTest {
  @Test
  fun `Back maps to device index 0`() {
    assertEquals(0, CamSelector.Back.deviceIndex)
  }

  @Test
  fun `Front also maps to device index 0 on desktop`() {
    assertEquals(0, CamSelector.Front.deviceIndex)
    assertEquals(CamSelector.Back.deviceIndex, CamSelector.Front.deviceIndex)
  }

  @Test
  fun `equality is based on camPosition and camLensTypes`() {
    val a = CamSelector(CamPosition.Back)
    val b = CamSelector(CamPosition.Back)
    val c = CamSelector(CamPosition.Front)
    assertEquals(a, b)
    assertNotEquals(a, c)
  }

  @Test
  fun `equal instances have equal hashCodes`() {
    val a = CamSelector(CamPosition.Back)
    val b = CamSelector(CamPosition.Back)
    assertEquals(a.hashCode(), b.hashCode())
  }

  @Test
  fun `CamSelector from cameraDevice uses deviceId as index`() {
    val cameraId = CameraId("2")
    val device = CameraDevice(
      cameraId = cameraId,
      name = "Test Camera",
      position = CamPosition.External,
      fov = 90f,
      lensType = listOf(CamLensType.Wide),
      photoData = emptyList(),
      videoData = emptyList(),
    )
    val selector = CamSelector(device)
    assertEquals(2, selector.deviceIndex)
  }

  @Test
  fun `CamSelector from cameraDevice with non-numeric id falls back to 0`() {
    val cameraId = CameraId("webcam-usb")
    val device = CameraDevice(
      cameraId = cameraId,
      name = "USB Camera",
      position = CamPosition.External,
      fov = 90f,
      lensType = listOf(CamLensType.Wide),
      photoData = emptyList(),
      videoData = emptyList(),
    )
    val selector = CamSelector(device)
    assertEquals(0, selector.deviceIndex)
  }
}
