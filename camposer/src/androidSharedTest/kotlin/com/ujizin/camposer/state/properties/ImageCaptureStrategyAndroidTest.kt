package com.ujizin.camposer.state.properties

import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalZeroShutterLag::class)
internal class ImageCaptureStrategyAndroidTest {
  @Test
  fun test_min_latency_mode_maps_to_zero_shutter_lag() {
    assertEquals(ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG, ImageCaptureStrategy.MinLatency.mode)
  }

  @Test
  fun test_max_quality_mode_maps_to_maximize_quality() {
    assertEquals(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY, ImageCaptureStrategy.MaxQuality.mode)
  }

  @Test
  fun test_balanced_mode_maps_to_minimize_latency() {
    assertEquals(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY, ImageCaptureStrategy.Balanced.mode)
  }

  @Test
  fun test_min_latency_fallback_maps_to_minimize_latency() {
    assertEquals(
      ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY,
      ImageCaptureStrategy.MinLatency.fallback,
    )
  }

  @Test
  fun test_max_quality_fallback_maps_to_maximize_quality() {
    assertEquals(
      ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY,
      ImageCaptureStrategy.MaxQuality.fallback,
    )
  }

  @Test
  fun test_balanced_fallback_maps_to_minimize_latency() {
    assertEquals(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY, ImageCaptureStrategy.Balanced.fallback)
  }

  @Test
  fun test_all_strategies_covered() {
    ImageCaptureStrategy.entries.forEach { strategy ->
      strategy.mode // must not throw
      strategy.fallback
    }
  }
}
