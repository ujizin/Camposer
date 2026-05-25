package com.ujizin.camposer.session

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CameraTorchEnabledTest : CameraSessionTest() {
  @Test
  fun test_preview_torch_enabled() {
    initCameraSession()

    val result = controller.setTorchEnabled(true)

    assertTrue(result.isSuccess)
    cameraTest.assertTorchEnabled(true)
    assertTrue(cameraSession.state.isTorchEnabled.value)
  }

  @Test
  fun test_preview_torch_disabled_after_enabled() {
    initCameraSession()
    assertTrue(controller.setTorchEnabled(true).isSuccess)

    val result = controller.setTorchEnabled(false)

    assertTrue(result.isSuccess)
    cameraTest.assertTorchEnabled(false)
    assertFalse(cameraSession.state.isTorchEnabled.value)
  }

  @Test
  fun test_preview_torch_enabled_with_no_support() {
    cameraTest.isFlashSupported = false
    initCameraSession()

    val result = controller.setTorchEnabled(true)

    assertTrue(result.isFailure)
    cameraTest.assertTorchEnabled(false)
    assertFalse(cameraSession.state.isTorchEnabled.value)
  }

  @Test
  fun test_preview_torch_disabled_always_succeeds_without_support() {
    cameraTest.isFlashSupported = false
    initCameraSession()

    val result = controller.setTorchEnabled(false)

    assertTrue(result.isSuccess)
    cameraTest.assertTorchEnabled(false)
    assertFalse(cameraSession.state.isTorchEnabled.value)
  }
}
