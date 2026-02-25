package com.ujizin.camposer.session

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class CameraZoomTest : CameraSessionTest() {
  @Test
  fun test_preview_zoom_change() =
    runTest {
      initCameraSession()
      val cameraInfoState = cameraSession.info.state.value

      val expectedZoom = 4F

      cameraSession.controller.setZoomRatio(expectedZoom)

      cameraTest.assertZoomRatio(expectedZoom)
      assertNotEquals(cameraInfoState.minZoom, cameraInfoState.maxZoom)
      assertEquals(cameraSession.state.zoomRatio.value, expectedZoom)
    }

  @Test
  fun test_preview_zoom_change_to_max() =
    runTest {
      initCameraSession()
      val cameraInfoState = cameraSession.info.state.value

      val expectedZoom = cameraInfoState.maxZoom

      cameraSession.controller.setZoomRatio(expectedZoom)

      cameraTest.assertZoomRatio(expectedZoom)
      assertNotEquals(cameraInfoState.minZoom, cameraInfoState.maxZoom)
      assertEquals(cameraInfoState.maxZoom, expectedZoom)
      assertEquals(cameraSession.state.zoomRatio.value, expectedZoom)
    }

  @Test
  fun test_preview_zoom_change_to_min() =
    runTest {
      initCameraSession()
      val cameraInfoState = cameraSession.info.state.value

      val expectedZoom = cameraInfoState.minZoom

      cameraSession.controller.setZoomRatio(expectedZoom)

      cameraTest.assertZoomRatio(expectedZoom)
      assertNotEquals(cameraInfoState.minZoom, cameraInfoState.maxZoom)
      assertEquals(cameraInfoState.minZoom, expectedZoom)
      assertEquals(cameraSession.state.zoomRatio.value, expectedZoom)
    }
}
