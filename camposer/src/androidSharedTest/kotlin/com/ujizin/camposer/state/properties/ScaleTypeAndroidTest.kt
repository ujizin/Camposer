package com.ujizin.camposer.state.properties

import androidx.camera.view.PreviewView
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ScaleTypeAndroidTest {
  @Test
  fun test_fit_start_maps_to_fit_start() {
    assertEquals(PreviewView.ScaleType.FIT_START, ScaleType.FitStart.type)
  }

  @Test
  fun test_fit_center_maps_to_fit_center() {
    assertEquals(PreviewView.ScaleType.FIT_CENTER, ScaleType.FitCenter.type)
  }

  @Test
  fun test_fit_end_maps_to_fit_end() {
    assertEquals(PreviewView.ScaleType.FIT_END, ScaleType.FitEnd.type)
  }

  @Test
  fun test_fill_start_maps_to_fill_start() {
    assertEquals(PreviewView.ScaleType.FILL_START, ScaleType.FillStart.type)
  }

  @Test
  fun test_fill_center_maps_to_fill_center() {
    assertEquals(PreviewView.ScaleType.FILL_CENTER, ScaleType.FillCenter.type)
  }

  @Test
  fun test_fill_end_maps_to_fill_end() {
    assertEquals(PreviewView.ScaleType.FILL_END, ScaleType.FillEnd.type)
  }
}
