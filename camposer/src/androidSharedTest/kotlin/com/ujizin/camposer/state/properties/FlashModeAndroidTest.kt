package com.ujizin.camposer.state.properties

import androidx.camera.core.ImageCapture
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FlashModeAndroidTest {
  @Test
  fun test_flash_mode_on_maps_to_flash_mode_on() {
    assertEquals(ImageCapture.FLASH_MODE_ON, FlashMode.On.mode)
  }

  @Test
  fun test_flash_mode_off_maps_to_flash_mode_off() {
    assertEquals(ImageCapture.FLASH_MODE_OFF, FlashMode.Off.mode)
  }

  @Test
  fun test_flash_mode_auto_maps_to_flash_mode_auto() {
    assertEquals(ImageCapture.FLASH_MODE_AUTO, FlashMode.Auto.mode)
  }

  @Test
  fun test_all_flash_modes_covered() {
    FlashMode.entries.forEach { mode ->
      mode.mode // must not throw
    }
  }

  @Test
  fun test_flash_mode_on_int_maps_to_flash_mode_on() {
    assertEquals(FlashMode.On, ImageCapture.FLASH_MODE_ON.toFlashMode())
  }

  @Test
  fun test_flash_mode_auto_int_maps_to_flash_mode_auto() {
    assertEquals(FlashMode.Auto, ImageCapture.FLASH_MODE_AUTO.toFlashMode())
  }

  @Test
  fun test_unknown_int_maps_to_flash_mode_off() {
    assertEquals(FlashMode.Off, (-1).toFlashMode())
  }

  @Test
  fun test_mode_and_to_flash_mode_are_inverse() {
    FlashMode.entries.forEach { mode ->
      assertEquals(mode, mode.mode.toFlashMode())
    }
  }
}
