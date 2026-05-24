package com.ujizin.camposer.session

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CameraFrameRateTest : CameraSessionTest() {
  @Test
  fun test_preview_frame_rate_set_to_max() {
    initCameraSession()
    val maxFps = cameraSession.info.state.value.maxFPS

    val result = controller.setVideoFrameRate(maxFps)

    assertTrue(result.isSuccess)
    cameraTest.assertFrameRate(maxFps)
    assertEquals(maxFps, cameraSession.state.frameRate.value)
  }

  @Test
  fun test_preview_frame_rate_set_to_min() {
    initCameraSession()
    val minFps = cameraSession.info.state.value.minFPS

    val result = controller.setVideoFrameRate(minFps)

    assertTrue(result.isSuccess)
    cameraTest.assertFrameRate(minFps)
    assertEquals(minFps, cameraSession.state.frameRate.value)
  }

  @Test
  fun test_preview_frame_rate_out_of_range_fails() {
    initCameraSession()
    val outOfRangeFps = cameraSession.info.state.value.maxFPS + 1

    val result = controller.setVideoFrameRate(outOfRangeFps)

    assertTrue(result.isFailure)
  }
}
