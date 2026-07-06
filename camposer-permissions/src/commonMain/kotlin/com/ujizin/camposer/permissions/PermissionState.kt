package com.ujizin.camposer.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/**
 * State holder for a single permission.
 *
 * Create instances with [rememberCameraPermissionState] or
 * [rememberAudioPermissionState].
 */
@Stable
public interface PermissionState {
  /** Current [PermissionStatus]. Backed by Compose state. */
  public val status: PermissionStatus

  /**
   * Launches the system permission dialog when possible.
   *
   * When the permission can no longer be requested
   * ([PermissionStatus.Denied.canRequestAgain] is `false`), this is a no-op;
   * use [openAppSettings] instead.
   */
  public fun launchPermissionRequest()

  /** Opens the app settings screen so the user can change the permission. */
  public fun openAppSettings()
}

/**
 * Creates and remembers a [PermissionState] for the camera permission.
 *
 * The status is refreshed automatically when the app returns to the foreground.
 *
 * @param onPermissionResult called with `true` when granted. Fires only for
 * explicit [PermissionState.launchPermissionRequest] results, not for
 * lifecycle refreshes.
 */
@Composable
public expect fun rememberCameraPermissionState(
  onPermissionResult: (Boolean) -> Unit = {},
): PermissionState

/**
 * Creates and remembers a [PermissionState] for the microphone permission.
 *
 * The status is refreshed automatically when the app returns to the foreground.
 *
 * @param onPermissionResult called with `true` when granted. Fires only for
 * explicit [PermissionState.launchPermissionRequest] results, not for
 * lifecycle refreshes.
 */
@Composable
public expect fun rememberAudioPermissionState(
  onPermissionResult: (Boolean) -> Unit = {},
): PermissionState
