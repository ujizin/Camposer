package com.ujizin.camposer.state.properties.selector

import com.ujizin.camposer.manager.CameraDevice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class CamSelectorJvmTest {
  private fun device(index: Int) =
    CameraDevice(
      cameraId = CameraId(index),
      name = "Camera $index",
      position = CamPosition.External,
      fov = 0F,
      lensType = listOf(CamLensType.Wide),
      photoData = emptyList(),
      videoData = emptyList(),
    )

  @Test
  fun test_front_and_back_open_default_device() {
    assertEquals(0, CamSelector.Front.deviceIndex)
    assertEquals(0, CamSelector.Back.deviceIndex)
  }

  @Test
  fun test_selector_from_device_uses_its_index() {
    val selector = CamSelector(device(2))

    assertEquals(2, selector.deviceIndex)
    assertEquals(CamPosition.External, selector.camPosition)
    assertEquals(listOf(CamLensType.Wide), selector.camLensTypes)
  }

  @Test
  fun test_empty_lens_list_falls_back_to_wide() {
    val selector = CamSelector(CamPosition.Back, emptyList())

    assertEquals(listOf(CamLensType.Wide), selector.camLensTypes)
  }

  @Test
  fun test_equality_includes_device_index() {
    assertEquals(CamSelector(device(1)), CamSelector(device(1)))
    assertEquals(CamSelector(device(1)).hashCode(), CamSelector(device(1)).hashCode())
    assertNotEquals(CamSelector(device(1)), CamSelector(device(2)))
    assertNotEquals(CamSelector.Front, CamSelector.Back)
  }

  @Test
  fun test_inverse_toggles_position() {
    assertEquals(CamPosition.Front, CamSelector.Back.inverse.camPosition)
    assertEquals(CamPosition.Back, CamSelector.Front.inverse.camPosition)
  }

  @Test
  fun test_to_string_contains_index() {
    assertEquals(
      "CamSelector(camPosition=External, camLensType=[Wide], deviceIndex=3)",
      CamSelector(device(3)).toString(),
    )
  }
}
