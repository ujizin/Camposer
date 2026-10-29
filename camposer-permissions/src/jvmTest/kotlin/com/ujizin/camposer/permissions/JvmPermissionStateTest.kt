package com.ujizin.camposer.permissions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class JvmPermissionStateTest {
  @Test
  fun test_status_is_always_granted() {
    val state = JvmPermissionState()

    assertEquals(PermissionStatus.Granted, state.status)
    assertTrue(state.status.isGranted)
  }

  @Test
  fun test_launch_permission_request_reports_granted() {
    val state = JvmPermissionState()
    var result: Boolean? = null
    state.onPermissionResult = { result = it }

    state.launchPermissionRequest()

    assertEquals(true, result)
    assertEquals(PermissionStatus.Granted, state.status)
  }

  @Test
  fun test_open_app_settings_is_noop() {
    val state = JvmPermissionState()

    state.openAppSettings()

    assertEquals(PermissionStatus.Granted, state.status)
  }
}
