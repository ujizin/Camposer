package com.ujizin.camposer.state.properties.selector

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CamLensTypeAndroidTest {
  @Test
  fun test_wide_min_fov() {
    assertEquals(61F, CamLensType.Wide.minFov)
  }

  @Test
  fun test_ultrawide_min_fov() {
    assertEquals(94F, CamLensType.UltraWide.minFov)
  }

  @Test
  fun test_telephoto_min_fov_is_zero() {
    assertEquals(0F, CamLensType.Telephoto.minFov)
  }

  @Test
  fun test_ultrawide_has_higher_fov_than_wide() {
    assertTrue(CamLensType.UltraWide.minFov > CamLensType.Wide.minFov)
  }

  @Test
  fun test_telephoto_has_lower_fov_than_wide() {
    assertTrue(CamLensType.Wide.minFov > CamLensType.Telephoto.minFov)
  }
}
