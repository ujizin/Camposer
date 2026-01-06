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

      val expectedZoom = 4F

      cameraSession.controller.setZoomRatio(expectedZoom)

      cameraTest.assertZoomRatio(expectedZoom)
      assertNotEquals(cameraSession.info.minZoom, cameraSession.info.maxZoom)
      assertEquals(cameraSession.state.zoomRatio, expectedZoom)
    }

  @Test
  fun test_preview_zoom_change_to_max() =
    runTest {
      initCameraSession()

      val expectedZoom = cameraSession.info.maxZoom

      cameraSession.controller.setZoomRatio(expectedZoom)

      cameraTest.assertZoomRatio(expectedZoom)
      assertNotEquals(cameraSession.info.minZoom, cameraSession.info.maxZoom)
      assertEquals(cameraSession.info.maxZoom, expectedZoom)
      assertEquals(cameraSession.state.zoomRatio, expectedZoom)
    }

  @Test
  fun test_preview_zoom_change_to_min() =
    runTest {
      initCameraSession()

      val expectedZoom = cameraSession.info.minZoom

      cameraSession.controller.setZoomRatio(expectedZoom)

      cameraTest.assertZoomRatio(expectedZoom)
      assertNotEquals(cameraSession.info.minZoom, cameraSession.info.maxZoom)
      assertEquals(cameraSession.info.minZoom, expectedZoom)
      assertEquals(cameraSession.state.zoomRatio, expectedZoom)
    }
}
