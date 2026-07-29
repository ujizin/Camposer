package com.ujizin.camposer.state.properties.selector

import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTelephotoCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTripleCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInUltraWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureDeviceTypeExternal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class CamSelectorIosTest {
  @Test
  fun test_wide_only_device_types() {
    val selector = CamSelector(CamPosition.Back, listOf(CamLensType.Wide))
    assertEquals(listOf(AVCaptureDeviceTypeBuiltInWideAngleCamera), selector.getDeviceTypes())
  }

  @Test
  fun test_empty_lens_types_defaults_to_wide() {
    val selector = CamSelector(CamPosition.Back, emptyList())
    assertEquals(listOf(AVCaptureDeviceTypeBuiltInWideAngleCamera), selector.getDeviceTypes())
  }

  @Test
  fun test_wide_and_telephoto_device_types() {
    val selector = CamSelector(CamPosition.Back, listOf(CamLensType.Wide, CamLensType.Telephoto))
    assertEquals(
      listOf(
        AVCaptureDeviceTypeBuiltInDualCamera,
        AVCaptureDeviceTypeBuiltInWideAngleCamera,
        AVCaptureDeviceTypeBuiltInTelephotoCamera,
      ),
      selector.getDeviceTypes(),
    )
  }

  @Test
  fun test_wide_and_ultrawide_device_types() {
    val selector = CamSelector(CamPosition.Back, listOf(CamLensType.Wide, CamLensType.UltraWide))
    assertEquals(
      listOf(
        AVCaptureDeviceTypeBuiltInDualWideCamera,
        AVCaptureDeviceTypeBuiltInWideAngleCamera,
        AVCaptureDeviceTypeBuiltInUltraWideCamera,
      ),
      selector.getDeviceTypes(),
    )
  }

  @Test
  fun test_all_lens_types_device_types() {
    val selector = CamSelector(
      CamPosition.Back,
      listOf(CamLensType.Wide, CamLensType.UltraWide, CamLensType.Telephoto),
    )
    assertEquals(
      listOf(
        AVCaptureDeviceTypeBuiltInTripleCamera,
        AVCaptureDeviceTypeBuiltInDualWideCamera,
        AVCaptureDeviceTypeBuiltInDualCamera,
        AVCaptureDeviceTypeBuiltInWideAngleCamera,
        AVCaptureDeviceTypeBuiltInUltraWideCamera,
        AVCaptureDeviceTypeBuiltInTelephotoCamera,
      ),
      selector.getDeviceTypes(),
    )
  }

  @Test
  fun test_external_position_includes_external_device_type() {
    val selector = CamSelector(CamPosition.External, listOf(CamLensType.Wide))
    assertTrue(AVCaptureDeviceTypeExternal in selector.getDeviceTypes())
  }

  @Test
  fun test_non_external_position_excludes_external_device_type() {
    val selector = CamSelector(CamPosition.Back, listOf(CamLensType.Wide))
    assertTrue(AVCaptureDeviceTypeExternal !in selector.getDeviceTypes())
  }

  @Test
  fun test_device_types_are_distinct() {
    val selector = CamSelector(
      CamPosition.Back,
      listOf(CamLensType.Wide, CamLensType.UltraWide, CamLensType.Telephoto),
    )
    val types = selector.getDeviceTypes()
    assertEquals(types.size, types.distinct().size)
  }

  @Test
  fun test_front_selector_equals_cam_selector_front() {
    assertEquals(CamSelector.Front, CamSelector(CamPosition.Front))
  }

  @Test
  fun test_back_selector_equals_cam_selector_back() {
    assertEquals(CamSelector.Back, CamSelector(CamPosition.Back))
  }

  @Test
  fun test_selectors_with_different_positions_not_equal() {
    assertNotEquals(CamSelector.Front, CamSelector.Back)
  }

  @Test
  fun test_selectors_with_different_lens_types_not_equal() {
    val wide = CamSelector(CamPosition.Back, listOf(CamLensType.Wide))
    val telephoto = CamSelector(CamPosition.Back, listOf(CamLensType.Telephoto))
    assertNotEquals(wide, telephoto)
  }
}
