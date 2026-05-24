package com.ujizin.camposer.state.properties.selector

import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDevicePositionUnspecified
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CamPositionIosTest {
  @Test
  fun test_back_maps_to_av_back() {
    assertEquals(AVCaptureDevicePositionBack, CamPosition.Back.value)
  }

  @Test
  fun test_front_maps_to_av_front() {
    assertEquals(AVCaptureDevicePositionFront, CamPosition.Front.value)
  }

  @Test
  fun test_external_maps_to_av_unspecified() {
    assertEquals(AVCaptureDevicePositionUnspecified, CamPosition.External.value)
  }

  @Test
  fun test_unknown_maps_to_av_unspecified() {
    assertEquals(AVCaptureDevicePositionUnspecified, CamPosition.Unknown.value)
  }
}
