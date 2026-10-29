package com.ujizin.camposer.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember

/**
 * Desktop has no runtime permission dialog that an application can drive. Camera and
 * microphone access is granted by the operating system when the device opens (macOS shows
 * its own prompt on first use). The state therefore always reports [PermissionStatus.Granted].
 */
@Composable
public actual fun rememberCameraPermissionState(
  onPermissionResult: (Boolean) -> Unit,
): PermissionState = rememberGrantedPermissionState(onPermissionResult)

/**
 * Desktop has no runtime permission dialog that an application can drive. The state always
 * reports [PermissionStatus.Granted].
 */
@Composable
public actual fun rememberAudioPermissionState(
  onPermissionResult: (Boolean) -> Unit,
): PermissionState = rememberGrantedPermissionState(onPermissionResult)

@Composable
private fun rememberGrantedPermissionState(
  onPermissionResult: (Boolean) -> Unit,
): PermissionState {
  val state = remember { JvmPermissionState() }
  SideEffect { state.onPermissionResult = onPermissionResult }
  return state
}

internal class JvmPermissionState : PermissionState {
  var onPermissionResult: (Boolean) -> Unit = {}

  override val status: PermissionStatus = PermissionStatus.Granted

  override fun launchPermissionRequest() {
    onPermissionResult(true)
  }

  override fun openAppSettings() {
    // No application settings screen exists for permissions on desktop.
  }
}
