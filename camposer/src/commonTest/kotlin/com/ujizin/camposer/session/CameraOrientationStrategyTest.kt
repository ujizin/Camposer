package com.ujizin.camposer.session

import com.ujizin.camposer.state.properties.OrientationStrategy
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CameraOrientationStrategyTest : CameraSessionTest() {
  @Test
  fun test_orientation_strategy_default_is_device() {
    initCameraSession()

    assertEquals(OrientationStrategy.Device, cameraSession.state.orientationStrategy.value)
  }

  @Test
  fun test_orientation_strategy_all_entries() {
    initCameraSession()

    OrientationStrategy.entries.forEach { strategy ->
      controller.setOrientationStrategy(strategy)

      assertEquals(strategy, cameraSession.state.orientationStrategy.value)
    }
  }

  @Test
  fun test_orientation_strategy_idempotent() {
    initCameraSession()

    controller.setOrientationStrategy(OrientationStrategy.Preview)
    assertEquals(OrientationStrategy.Preview, cameraSession.state.orientationStrategy.value)

    controller.setOrientationStrategy(OrientationStrategy.Preview)
    assertEquals(OrientationStrategy.Preview, cameraSession.state.orientationStrategy.value)
  }
}
