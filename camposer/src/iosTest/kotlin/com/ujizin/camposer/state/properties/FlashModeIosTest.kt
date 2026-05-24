package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVCaptureFlashModeAuto
import platform.AVFoundation.AVCaptureFlashModeOff
import platform.AVFoundation.AVCaptureFlashModeOn
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FlashModeIosTest {
  @Test
  fun test_flash_mode_on_maps_to_av_on() {
    assertEquals(AVCaptureFlashModeOn, FlashMode.On.mode)
  }

  @Test
  fun test_flash_mode_off_maps_to_av_off() {
    assertEquals(AVCaptureFlashModeOff, FlashMode.Off.mode)
  }

  @Test
  fun test_flash_mode_auto_maps_to_av_auto() {
    assertEquals(AVCaptureFlashModeAuto, FlashMode.Auto.mode)
  }

  @Test
  fun test_all_flash_modes_covered() {
    FlashMode.entries.forEach { mode ->
      mode.mode // must not throw
    }
  }

  @Test
  fun test_av_on_maps_to_flash_mode_on() {
    assertEquals(FlashMode.On, AVCaptureFlashModeOn.toFlashMode())
  }

  @Test
  fun test_av_off_maps_to_flash_mode_off() {
    assertEquals(FlashMode.Off, AVCaptureFlashModeOff.toFlashMode())
  }

  @Test
  fun test_av_auto_maps_to_flash_mode_auto() {
    assertEquals(FlashMode.Auto, AVCaptureFlashModeAuto.toFlashMode())
  }

  @Test
  fun test_mode_and_to_flash_mode_are_inverse() {
    FlashMode.entries.forEach { mode ->
      assertEquals(mode, mode.mode.toFlashMode())
    }
  }
}
