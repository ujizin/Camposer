package com.ujizin.camposer.session

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CameraPinchToZoomEnabledTest : CameraSessionTest() {
  @Test
  fun test_preview_pinch_to_zoom_enabled_default_is_true() {
    initCameraSession()

    assertTrue(cameraSession.state.isPinchToZoomEnabled.value)
  }

  @Test
  fun test_preview_pinch_to_zoom_disabled() {
    updateSession(isPinchToZoomEnabled = false)

    assertFalse(cameraSession.state.isPinchToZoomEnabled.value)
  }

  @Test
  fun test_preview_pinch_to_zoom_re_enabled() {
    updateSession(isPinchToZoomEnabled = false)
    updateSession(isPinchToZoomEnabled = true)

    assertTrue(cameraSession.state.isPinchToZoomEnabled.value)
  }

  @Test
  fun test_preview_pinch_to_zoom_idempotent() {
    updateSession(isPinchToZoomEnabled = false)
    updateSession(isPinchToZoomEnabled = false)

    assertFalse(cameraSession.state.isPinchToZoomEnabled.value)
  }
}
