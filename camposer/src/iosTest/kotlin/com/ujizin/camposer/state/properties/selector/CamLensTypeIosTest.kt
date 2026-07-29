package com.ujizin.camposer.state.properties.selector

import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTelephotoCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTripleCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInUltraWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CamLensTypeIosTest {
  @Test
  fun test_wide_maps_to_wide_angle_camera() {
    assertEquals(AVCaptureDeviceTypeBuiltInWideAngleCamera, CamLensType.Wide.type)
  }

  @Test
  fun test_ultrawide_maps_to_ultrawide_camera() {
    assertEquals(AVCaptureDeviceTypeBuiltInUltraWideCamera, CamLensType.UltraWide.type)
  }

  @Test
  fun test_telephoto_maps_to_telephoto_camera() {
    assertEquals(AVCaptureDeviceTypeBuiltInTelephotoCamera, CamLensType.Telephoto.type)
  }

  @Test
  fun test_wide_angle_reverse_maps_to_wide() {
    assertEquals(
      listOf(CamLensType.Wide),
      getPhysicalLensByVirtual(AVCaptureDeviceTypeBuiltInWideAngleCamera),
    )
  }

  @Test
  fun test_ultrawide_reverse_maps_to_ultrawide() {
    assertEquals(
      listOf(CamLensType.UltraWide),
      getPhysicalLensByVirtual(AVCaptureDeviceTypeBuiltInUltraWideCamera),
    )
  }

  @Test
  fun test_telephoto_reverse_maps_to_telephoto() {
    assertEquals(
      listOf(CamLensType.Telephoto),
      getPhysicalLensByVirtual(AVCaptureDeviceTypeBuiltInTelephotoCamera),
    )
  }

  @Test
  fun test_dual_wide_camera_reverse_maps_to_wide_and_ultrawide() {
    assertEquals(
      listOf(CamLensType.Wide, CamLensType.UltraWide),
      getPhysicalLensByVirtual(AVCaptureDeviceTypeBuiltInDualWideCamera),
    )
  }

  @Test
  fun test_dual_camera_reverse_maps_to_wide_and_telephoto() {
    assertEquals(
      listOf(CamLensType.Wide, CamLensType.Telephoto),
      getPhysicalLensByVirtual(AVCaptureDeviceTypeBuiltInDualCamera),
    )
  }

  @Test
  fun test_triple_camera_reverse_maps_to_all_lens_types() {
    assertEquals(
      listOf(CamLensType.Wide, CamLensType.UltraWide, CamLensType.Telephoto),
      getPhysicalLensByVirtual(AVCaptureDeviceTypeBuiltInTripleCamera),
    )
  }

  @Test
  fun test_unknown_device_type_returns_empty() {
    assertTrue(getPhysicalLensByVirtual("com.unknown.type").isEmpty())
  }
}
