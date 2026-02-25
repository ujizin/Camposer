package com.ujizin.camposer.session

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CameraExposureCompensationTest : CameraSessionTest() {
  @Test
  fun test_preview_exposure_changed() {
    initCameraSession()

    val expected = 4.5F
    controller.setExposureCompensation(expected)

    assertExposureCompensation(expected)
    assertTrue(cameraSession.info.state.value.isExposureSupported)
  }

  @Test
  fun test_preview_exposure_changed_to_max() {
    initCameraSession()
    val cameraInfoState = cameraSession.info.state.value

    val expected = cameraInfoState.maxExposure
    controller.setExposureCompensation(expected)

    assertExposureCompensation(expected)
    assertTrue(cameraInfoState.isExposureSupported)
  }

  @Test
  fun test_preview_exposure_changed_to_min() {
    initCameraSession()
    val cameraInfoState = cameraSession.info.state.value

    val expected = cameraInfoState.minExposure
    controller.setExposureCompensation(expected)

    assertExposureCompensation(expected)
    assertTrue(cameraInfoState.isExposureSupported)
  }

  @Test
  fun test_preview_exposure_try_change_with_no_support() {
    cameraTest.isExposureSupported = false

    initCameraSession()

    assertFalse(cameraSession.info.state.value.isExposureSupported)
  }

  private fun assertExposureCompensation(exposureCompensation: Float) {
    cameraTest.assertExposureCompensation(exposureCompensation)
    assertEquals(cameraSession.state.exposureCompensation.value, exposureCompensation)
  }
}
