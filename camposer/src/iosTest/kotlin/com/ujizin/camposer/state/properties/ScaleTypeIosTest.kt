package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ScaleTypeIosTest {
  @Test
  fun test_fit_start_maps_to_resize_aspect() {
    assertEquals(AVLayerVideoGravityResizeAspect, ScaleType.FitStart.gravity)
  }

  @Test
  fun test_fit_center_maps_to_resize_aspect() {
    assertEquals(AVLayerVideoGravityResizeAspect, ScaleType.FitCenter.gravity)
  }

  @Test
  fun test_fit_end_maps_to_resize_aspect() {
    assertEquals(AVLayerVideoGravityResizeAspect, ScaleType.FitEnd.gravity)
  }

  @Test
  fun test_fill_start_maps_to_resize_aspect_fill() {
    assertEquals(AVLayerVideoGravityResizeAspectFill, ScaleType.FillStart.gravity)
  }

  @Test
  fun test_fill_center_maps_to_resize_aspect_fill() {
    assertEquals(AVLayerVideoGravityResizeAspectFill, ScaleType.FillCenter.gravity)
  }

  @Test
  fun test_fill_end_maps_to_resize_aspect_fill() {
    assertEquals(AVLayerVideoGravityResizeAspectFill, ScaleType.FillEnd.gravity)
  }
}
