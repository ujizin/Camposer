package com.ujizin.camposer.state.properties

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ImageCaptureStrategyJvmTest {
  @Test
  fun test_jpeg_quality_mapping() {
    assertEquals(75, ImageCaptureStrategy.MinLatency.jpegQuality)
    assertEquals(90, ImageCaptureStrategy.Balanced.jpegQuality)
    assertEquals(100, ImageCaptureStrategy.MaxQuality.jpegQuality)
  }
}
