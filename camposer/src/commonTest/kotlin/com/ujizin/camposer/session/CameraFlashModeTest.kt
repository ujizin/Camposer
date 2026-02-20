package com.ujizin.camposer.session

import com.ujizin.camposer.state.properties.FlashMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CameraFlashModeTest : CameraSessionTest() {
  @Test
  fun test_preview_flash_mode_on() {
    initCameraSession()

    val expectedFlashMode = FlashMode.On

    controller.setFlashMode(expectedFlashMode)

    assertFlashMode(expectedFlashMode)
    assertTrue(cameraSession.info.isFlashSupported)
  }

  @Test
  fun test_preview_flash_mode_off_after_on() {
    initCameraSession()

    controller.setFlashMode(FlashMode.On)

    assertFlashMode(FlashMode.On)

    val expectedFlashMode = FlashMode.Off
    controller.setFlashMode(expectedFlashMode)

    assertFlashMode(FlashMode.Off)
    assertTrue(cameraSession.info.isFlashSupported)
  }

  @Test
  fun test_preview_flash_mode_auto() {
    initCameraSession()

    val expectedFlashMode = FlashMode.Auto

    controller.setFlashMode(expectedFlashMode)

    assertFlashMode(expectedFlashMode)
    assertTrue(cameraSession.info.isFlashSupported)
  }

  @Test
  fun test_preview_all_flash_mode() {
    initCameraSession()

    FlashMode.entries.forEach { expected ->
      controller.setFlashMode(expected)

      assertFlashMode(expected)
      assertTrue(cameraSession.info.isFlashSupported)
    }
  }

  @Test
  fun test_preview_flash_mode_on_with_no_support() {
    cameraTest.isFlashSupported = false

    initCameraSession()

    val result = controller.setFlashMode(FlashMode.On)

    assertTrue(result.isFailure)
    assertFalse(cameraSession.info.isFlashSupported)
    assertFlashMode(FlashMode.Off)
  }

  @Test
  fun test_preview_flash_mode_auto_with_no_support() {
    cameraTest.isFlashSupported = false

    initCameraSession()

    val result = controller.setFlashMode(FlashMode.Auto)

    assertTrue(result.isFailure)
    assertFalse(cameraSession.info.isFlashSupported)
    assertFlashMode(FlashMode.Off)
  }

  @Test
  fun test_preview_flash_mode_off_with_no_support() {
    cameraTest.isFlashSupported = false

    initCameraSession()

    val result = controller.setFlashMode(FlashMode.Off)

    assertTrue(result.isSuccess)
    assertFalse(cameraSession.info.isFlashSupported)
    assertFlashMode(FlashMode.Off)
  }

  private fun assertFlashMode(expectedFlashMode: FlashMode) {
    cameraTest.assertFlashMode(expectedFlashMode)
    assertEquals(expectedFlashMode, cameraSession.state.flashMode.value)
  }
}
