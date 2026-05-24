package com.ujizin.camposer.internal.extensions

import platform.AVFoundation.AVCaptureVideoOrientationLandscapeLeft
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoOrientationPortraitUpsideDown
import platform.UIKit.UIInterfaceOrientationLandscapeLeft
import platform.UIKit.UIInterfaceOrientationLandscapeRight
import platform.UIKit.UIInterfaceOrientationPortrait
import platform.UIKit.UIInterfaceOrientationPortraitUpsideDown
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AVCaptureOrientationTest {
  @Test
  fun test_landscape_left_maps_to_av_landscape_left() {
    assertEquals(
      AVCaptureVideoOrientationLandscapeLeft,
      UIInterfaceOrientationLandscapeLeft.toVideoOrientation(),
    )
  }

  @Test
  fun test_landscape_right_maps_to_av_landscape_right() {
    assertEquals(
      AVCaptureVideoOrientationLandscapeRight,
      UIInterfaceOrientationLandscapeRight.toVideoOrientation(),
    )
  }

  @Test
  fun test_portrait_upside_down_maps_to_av_portrait_upside_down() {
    assertEquals(
      AVCaptureVideoOrientationPortraitUpsideDown,
      UIInterfaceOrientationPortraitUpsideDown.toVideoOrientation(),
    )
  }

  @Test
  fun test_portrait_maps_to_av_portrait() {
    assertEquals(
      AVCaptureVideoOrientationPortrait,
      UIInterfaceOrientationPortrait.toVideoOrientation(),
    )
  }

  @Test
  fun test_unknown_orientation_falls_back_to_portrait() {
    val unknown = 0L // UIInterfaceOrientationUnknown raw value
    assertEquals(
      AVCaptureVideoOrientationPortrait,
      unknown.toVideoOrientation(),
    )
  }
}
