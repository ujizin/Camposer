package com.ujizin.camposer.permissions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PermissionStatusTest {
  @Test
  fun grantedStatusIsGrantedReturnsTrue() {
    assertTrue(PermissionStatus.Granted.isGranted)
  }

  @Test
  fun deniedStatusIsGrantedReturnsFalse() {
    assertFalse(PermissionStatus.Denied(canRequestAgain = true).isGranted)
    assertFalse(PermissionStatus.Denied(canRequestAgain = false).isGranted)
  }

  @Test
  fun deniedStatusesWithSameCanRequestAgainAreEqual() {
    assertEquals(
      PermissionStatus.Denied(canRequestAgain = true),
      PermissionStatus.Denied(canRequestAgain = true),
    )
  }
}
