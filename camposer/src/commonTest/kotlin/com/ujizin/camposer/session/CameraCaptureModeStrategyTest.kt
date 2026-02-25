package com.ujizin.camposer.session

import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CameraCaptureModeStrategyTest : CameraSessionTest() {
  @Test
  fun test_preview_all_strategy() {
    ImageCaptureStrategy.entries.forEach { strategy ->
      updateSession(imageCaptureStrategy = strategy)

      cameraTest.assertImageCaptureStrategy(strategy)
      assertEquals(strategy, cameraSession.state.imageCaptureStrategy.value)
    }
  }

  @Test
  fun test_preview_min_latency_strategy() {
    val strategy = ImageCaptureStrategy.MinLatency
    updateSession(imageCaptureStrategy = strategy)

    cameraTest.assertImageCaptureStrategy(strategy)
    assertEquals(strategy, cameraSession.state.imageCaptureStrategy.value)
  }

  @Test
  fun test_preview_min_latency_strategy_with_no_ZSL_supported_android() {
    cameraTest.isZSLSupported = false
    updateSession(imageCaptureStrategy = ImageCaptureStrategy.MinLatency)

    cameraTest.assertImageCaptureStrategy(ImageCaptureStrategy.MinLatency)
    assertEquals(ImageCaptureStrategy.MinLatency, cameraSession.state.imageCaptureStrategy.value)
  }
}
