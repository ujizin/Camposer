package com.ujizin.camposer.permissions

import platform.AVFoundation.AVAuthorizationStatus
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined

internal fun AVAuthorizationStatus.toPermissionStatus(): PermissionStatus =
  when (this) {
    AVAuthorizationStatusAuthorized -> PermissionStatus.Granted
    AVAuthorizationStatusNotDetermined -> PermissionStatus.Denied(canRequestAgain = true)
    else -> PermissionStatus.Denied(canRequestAgain = false)
  }
