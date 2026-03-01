package com.ujizin.camposer.controller.camera

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

internal class CameraControllerTest {

  @Test
  fun `controller can be constructed`() {
    val controller = CameraController()
    assertNotNull(controller)
  }

  @Test
  fun `controller isRunning is false before initialization`() {
    val controller = CameraController()
    assertFalse(controller.isRunning.value)
  }
}
