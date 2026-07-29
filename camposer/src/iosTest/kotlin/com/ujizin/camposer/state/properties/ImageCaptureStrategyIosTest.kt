package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVCapturePhotoQualityPrioritizationBalanced
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationQuality
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationSpeed
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ImageCaptureStrategyIosTest {
  @Test
  fun test_min_latency_quality_maps_to_speed() {
    assertEquals(AVCapturePhotoQualityPrioritizationSpeed, ImageCaptureStrategy.MinLatency.quality)
  }

  @Test
  fun test_max_quality_quality_maps_to_quality() {
    assertEquals(
      AVCapturePhotoQualityPrioritizationQuality,
      ImageCaptureStrategy.MaxQuality.quality,
    )
  }

  @Test
  fun test_balanced_quality_maps_to_balanced() {
    assertEquals(AVCapturePhotoQualityPrioritizationBalanced, ImageCaptureStrategy.Balanced.quality)
  }

  @Test
  fun test_min_latency_high_resolution_disabled() {
    assertFalse(ImageCaptureStrategy.MinLatency.highResolutionEnabled)
  }

  @Test
  fun test_max_quality_high_resolution_enabled() {
    assertTrue(ImageCaptureStrategy.MaxQuality.highResolutionEnabled)
  }

  @Test
  fun test_balanced_high_resolution_disabled() {
    assertFalse(ImageCaptureStrategy.Balanced.highResolutionEnabled)
  }

  @Test
  fun test_all_strategies_covered() {
    ImageCaptureStrategy.entries.forEach { strategy ->
      strategy.quality // must not throw
      strategy.highResolutionEnabled
    }
  }
}
