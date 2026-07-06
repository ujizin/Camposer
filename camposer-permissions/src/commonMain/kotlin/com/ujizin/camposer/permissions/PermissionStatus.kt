package com.ujizin.camposer.permissions

/**
 * Represents the status of a camera or microphone permission.
 */
public sealed interface PermissionStatus {

  /** Permission is granted. */
  public data object Granted : PermissionStatus

  /**
   * Permission is denied or not requested yet.
   *
   * @param canRequestAgain whether calling
   * [PermissionState.launchPermissionRequest] can still show the system dialog.
   * When `false`, the user must grant the permission from the app settings,
   * see [PermissionState.openAppSettings].
   */
  public data class Denied(val canRequestAgain: Boolean) : PermissionStatus
}

/** Returns `true` when the permission is granted. */
public val PermissionStatus.isGranted: Boolean
  get() = this == PermissionStatus.Granted
