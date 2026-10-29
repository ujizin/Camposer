package com.ujizin.camposer.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Desktop camera device manager.
 *
 * Devices are discovered once when the manager is created. Desktop backends offer no
 * hot-plug notification, so a new manager must be created to pick up connected webcams.
 */
internal actual class CameraDevicesManager(
  private val discoverer: JvmCameraDeviceDiscoverer = ProbingCameraDeviceDiscoverer(),
  dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
  private val scope = CoroutineScope(SupervisorJob() + dispatcher)

  private val _cameraDevicesState = MutableStateFlow<CameraDeviceState>(CameraDeviceState.Initial)
  actual val cameraDevicesState: StateFlow<CameraDeviceState> = _cameraDevicesState.asStateFlow()

  init {
    scope.launch {
      val devices = discoverer.discoverDevices()
      _cameraDevicesState.update { CameraDeviceState.Devices(devices) }
    }
  }

  actual fun release() {
    scope.cancel()
  }
}

@Composable
public actual fun rememberCameraDeviceState(): State<CameraDeviceState> {
  val cameraDevicesManager = remember { CameraDevicesManager() }
  val cameraDeviceState = cameraDevicesManager.cameraDevicesState.collectAsStateWithLifecycle()

  DisposableEffect(cameraDevicesManager) {
    onDispose { cameraDevicesManager.release() }
  }

  return cameraDeviceState
}
