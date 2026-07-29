package com.ujizin.camposer.session

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CameraFocusOnTapEnabledTest : CameraSessionTest() {
  @Test
  fun test_preview_focus_on_tap_default_is_true() {
    initCameraSession()

    assertTrue(cameraSession.state.isFocusOnTapEnabled.value)
  }

  @Test
  fun test_preview_focus_on_tap_disabled() {
    updateSession(isFocusOnTapEnabled = false)

    assertFalse(cameraSession.state.isFocusOnTapEnabled.value)
  }

  @Test
  fun test_preview_focus_on_tap_re_enabled() {
    updateSession(isFocusOnTapEnabled = false)
    updateSession(isFocusOnTapEnabled = true)

    assertTrue(cameraSession.state.isFocusOnTapEnabled.value)
  }

  @Test
  fun test_preview_focus_on_tap_idempotent() {
    updateSession(isFocusOnTapEnabled = false)
    updateSession(isFocusOnTapEnabled = false)

    assertFalse(cameraSession.state.isFocusOnTapEnabled.value)
  }
}
