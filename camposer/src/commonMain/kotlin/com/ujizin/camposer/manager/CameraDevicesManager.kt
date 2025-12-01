package com.ujizin.camposer.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages the state and lifecycle of available camera devices.
 *
 * This class is responsible for observing the availability of camera hardware
 * and exposing the current state of connected camera devices (e.g., back or front cameras).
 *
 * Being an `expect` class, it has platform-specific implementations (e.g., using `CameraManager` on Android)
 * to handle the low-level details of device discovery.
 *
 * @property cameraDevicesState A [StateFlow] emitting the current [CameraDeviceState], representing the status of available cameras.
 * @see rememberCameraDeviceState for a composable way to observe this state.
 */
public expect class CameraDevicesManager {
  public val cameraDevicesState: StateFlow<CameraDeviceState>

  public fun release()
}

@Composable
public expect fun rememberCameraDeviceState(): State<CameraDeviceState>
