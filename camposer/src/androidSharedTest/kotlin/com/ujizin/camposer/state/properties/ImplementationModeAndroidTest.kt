package com.ujizin.camposer.state.properties

import androidx.camera.view.PreviewView
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ImplementationModeAndroidTest {
  @Test
  fun test_compatible_maps_to_compatible() {
    assertEquals(PreviewView.ImplementationMode.COMPATIBLE, ImplementationMode.Compatible.value)
  }

  @Test
  fun test_performance_maps_to_performance() {
    assertEquals(PreviewView.ImplementationMode.PERFORMANCE, ImplementationMode.Performance.value)
  }

  @Test
  fun test_all_implementation_modes_covered() {
    ImplementationMode.entries.forEach { mode ->
      mode.value // must not throw
    }
  }
}
