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
    assertTrue(cameraSession.info.isExposureSupported)
  }

  @Test
  fun test_preview_exposure_changed_to_max() {
    initCameraSession()

    val expected = cameraSession.info.maxExposure
    controller.setExposureCompensation(expected)

    assertExposureCompensation(expected)
    assertTrue(cameraSession.info.isExposureSupported)
  }

  @Test
  fun test_preview_exposure_changed_to_min() {
    initCameraSession()

    val expected = cameraSession.info.minExposure
    controller.setExposureCompensation(expected)

    assertExposureCompensation(expected)
    assertTrue(cameraSession.info.isExposureSupported)
  }

  @Test
  fun test_preview_exposure_try_change_with_no_support() {
    cameraTest.isExposureSupported = false

    initCameraSession()

    assertFalse(cameraSession.info.isExposureSupported)
  }

  private fun assertExposureCompensation(exposureCompensation: Float) {
    cameraTest.assertExposureCompensation(exposureCompensation)
    assertEquals(cameraSession.state.exposureCompensation.value, exposureCompensation)
  }
}
