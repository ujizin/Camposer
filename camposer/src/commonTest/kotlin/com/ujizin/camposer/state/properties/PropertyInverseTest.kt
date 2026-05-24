package com.ujizin.camposer.state.properties

import kotlin.test.Test
import kotlin.test.assertEquals

internal class PropertyInverseTest {
  // ── FlashMode.inverse ─────────────────────────────────────────────────────────

  @Test
  fun test_flash_mode_on_inverse_is_off() {
    assertEquals(FlashMode.Off, FlashMode.On.inverse)
  }

  @Test
  fun test_flash_mode_off_inverse_is_on() {
    assertEquals(FlashMode.On, FlashMode.Off.inverse)
  }

  @Test
  fun test_flash_mode_auto_inverse_is_on() {
    assertEquals(FlashMode.On, FlashMode.Auto.inverse)
  }

  // ── ImplementationMode.inverse ────────────────────────────────────────────────

  @Test
  fun test_implementation_mode_compatible_inverse_is_performance() {
    assertEquals(ImplementationMode.Performance, ImplementationMode.Compatible.inverse)
  }

  @Test
  fun test_implementation_mode_performance_inverse_is_compatible() {
    assertEquals(ImplementationMode.Compatible, ImplementationMode.Performance.inverse)
  }
}
