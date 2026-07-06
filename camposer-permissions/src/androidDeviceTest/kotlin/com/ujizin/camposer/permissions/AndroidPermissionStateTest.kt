package com.ujizin.camposer.permissions

import android.Manifest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AndroidPermissionStateTest {

  @get:Rule
  val grantPermissionRule: GrantPermissionRule =
    GrantPermissionRule.grant(Manifest.permission.CAMERA)

  private val context = InstrumentationRegistry.getInstrumentation().targetContext

  @Test
  fun grantedPermission_statusIsGranted() {
    val state = AndroidPermissionState(Manifest.permission.CAMERA, context)

    assertEquals(PermissionStatus.Granted, state.status)
  }

  @Test
  fun notGrantedPermission_statusIsDeniedRequestable() {
    val state = AndroidPermissionState(Manifest.permission.RECORD_AUDIO, context)

    assertEquals(PermissionStatus.Denied(canRequestAgain = true), state.status)
  }

  @Test
  fun refreshStatus_keepsGranted() {
    val state = AndroidPermissionState(Manifest.permission.CAMERA, context)

    state.refreshStatus()

    assertEquals(PermissionStatus.Granted, state.status)
  }
}
