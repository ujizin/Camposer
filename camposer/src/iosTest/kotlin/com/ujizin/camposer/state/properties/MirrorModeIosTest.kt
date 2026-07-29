package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDevicePositionUnspecified
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class MirrorModeIosTest {
  @Test
  fun test_mirror_on_enabled_for_front() {
    assertTrue(MirrorMode.On.isMirrorEnabled(AVCaptureDevicePositionFront))
  }

  @Test
  fun test_mirror_on_enabled_for_back() {
    assertTrue(MirrorMode.On.isMirrorEnabled(AVCaptureDevicePositionBack))
  }

  @Test
  fun test_mirror_on_enabled_for_unspecified() {
    assertTrue(MirrorMode.On.isMirrorEnabled(AVCaptureDevicePositionUnspecified))
  }

  @Test
  fun test_mirror_off_disabled_for_front() {
    assertFalse(MirrorMode.Off.isMirrorEnabled(AVCaptureDevicePositionFront))
  }

  @Test
  fun test_mirror_off_disabled_for_back() {
    assertFalse(MirrorMode.Off.isMirrorEnabled(AVCaptureDevicePositionBack))
  }

  @Test
  fun test_mirror_off_disabled_for_unspecified() {
    assertFalse(MirrorMode.Off.isMirrorEnabled(AVCaptureDevicePositionUnspecified))
  }

  @Test
  fun test_mirror_only_in_front_enabled_for_front() {
    assertTrue(MirrorMode.OnlyInFront.isMirrorEnabled(AVCaptureDevicePositionFront))
  }

  @Test
  fun test_mirror_only_in_front_disabled_for_back() {
    assertFalse(MirrorMode.OnlyInFront.isMirrorEnabled(AVCaptureDevicePositionBack))
  }

  @Test
  fun test_mirror_only_in_front_disabled_for_unspecified() {
    assertFalse(MirrorMode.OnlyInFront.isMirrorEnabled(AVCaptureDevicePositionUnspecified))
  }
}
