package com.ujizin.camposer.permissions

import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import kotlin.test.Test
import kotlin.test.assertEquals

class PermissionStatusMapperTest {
  @Test
  fun authorizedMapsToGranted() {
    assertEquals(
      PermissionStatus.Granted,
      AVAuthorizationStatusAuthorized.toPermissionStatus(),
    )
  }

  @Test
  fun notDeterminedMapsToDeniedCanRequestAgain() {
    assertEquals(
      PermissionStatus.Denied(canRequestAgain = true),
      AVAuthorizationStatusNotDetermined.toPermissionStatus(),
    )
  }

  @Test
  fun deniedMapsToDeniedPermanent() {
    assertEquals(
      PermissionStatus.Denied(canRequestAgain = false),
      AVAuthorizationStatusDenied.toPermissionStatus(),
    )
  }

  @Test
  fun restrictedMapsToDeniedPermanent() {
    assertEquals(
      PermissionStatus.Denied(canRequestAgain = false),
      AVAuthorizationStatusRestricted.toPermissionStatus(),
    )
  }
}
