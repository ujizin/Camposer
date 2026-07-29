package com.ujizin.camposer.permissions

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import com.ujizin.camposer.permissions.fake.FakeActivity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// canRequestAgain asserted only where hardcoded; elsewhere it differs JVM vs device.
internal class AndroidPermissionStateHostTest {
  @Test
  fun grantedPermissionStartsAsGranted() {
    val state = AndroidPermissionState(PERMISSION, FakeActivity(isGranted = true))

    assertEquals(PermissionStatus.Granted, state.status)
  }

  @Test
  fun deniedPermissionStartsAsRequestableDenied() {
    val state = AndroidPermissionState(PERMISSION, FakeActivity(isGranted = false))

    assertEquals(PermissionStatus.Denied(canRequestAgain = true), state.status)
  }

  @Test
  fun refreshStatusReadsCurrentPermission() {
    val state = AndroidPermissionState(PERMISSION, FakeActivity(isGranted = true))

    state.refreshStatus()

    assertEquals(PermissionStatus.Granted, state.status)
  }

  @Test
  fun refreshStatusOnDeniedPermissionReportsDenied() {
    val state = AndroidPermissionState(PERMISSION, FakeActivity(isGranted = false))

    state.refreshStatus()

    assertIs<PermissionStatus.Denied>(state.status)
  }

  @Test
  fun grantedRequestResultNotifiesCallback() {
    val state = AndroidPermissionState(PERMISSION, FakeActivity(isGranted = false))
    var result: Boolean? = null
    state.onPermissionResult = { result = it }

    state.onRequestResult(granted = true)

    assertEquals(PermissionStatus.Granted, state.status)
    assertEquals(true, result)
  }

  @Test
  fun deniedRequestResultNotifiesCallback() {
    val state = AndroidPermissionState(PERMISSION, FakeActivity(isGranted = true))
    var result: Boolean? = null
    state.onPermissionResult = { result = it }

    state.onRequestResult(granted = false)

    assertIs<PermissionStatus.Denied>(state.status)
    assertEquals(false, result)
  }

  @Test
  fun launchPermissionRequestForwardsPermissionToLauncher() {
    val state = AndroidPermissionState(PERMISSION, FakeActivity(isGranted = false))
    val launcher = FakeLauncher()
    state.launcher = launcher

    state.launchPermissionRequest()

    assertEquals(PERMISSION, launcher.launchedInput)
  }

  @Test
  fun launchPermissionRequestWithoutLauncherIsNoOp() {
    val state = AndroidPermissionState(PERMISSION, FakeActivity(isGranted = false))

    state.launchPermissionRequest()

    assertNull(state.launcher)
  }

  @Test
  fun openAppSettingsStartsSettingsActivity() {
    val activity = FakeActivity(isGranted = false)
    val state = AndroidPermissionState(PERMISSION, activity)

    state.openAppSettings()

    assertNotNull(activity.startedIntent)
  }

  @Test
  fun statusIsGrantedMatchesPermissionState() {
    val state = AndroidPermissionState(PERMISSION, FakeActivity(isGranted = true))

    assertTrue(state.status.isGranted)
  }

  private class FakeLauncher : ActivityResultLauncher<String>() {
    var launchedInput: String? = null
      private set

    override fun launch(
      input: String,
      options: ActivityOptionsCompat?,
    ) {
      launchedInput = input
    }

    override fun unregister() = Unit

    override val contract: ActivityResultContract<String, *> =
      ActivityResultContracts.RequestPermission()
  }

  private companion object {
    const val PERMISSION = "android.permission.CAMERA"
  }
}
