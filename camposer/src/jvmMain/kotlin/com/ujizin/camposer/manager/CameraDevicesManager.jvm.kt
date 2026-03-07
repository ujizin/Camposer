package com.ujizin.camposer.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal actual class CameraDevicesManager(
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
  private val pollIntervalMs: Long = 3_000L,
  private val cameraDeviceDiscoverer: CameraDeviceDiscoverer = OpenCvCameraDeviceDiscoverer,
) {
  private val scope = CoroutineScope(dispatcher)
  private val _cameraDevicesState = MutableStateFlow<CameraDeviceState>(CameraDeviceState.Initial)
  actual val cameraDevicesState: StateFlow<CameraDeviceState> = _cameraDevicesState.asStateFlow()

  init {
    scope.launch { poll() }
  }

  private suspend fun poll() {
    while (true) {
      currentCoroutineContext().ensureActive()
      val discovered = cameraDeviceDiscoverer.discoverDevices()
      _cameraDevicesState.update { discovered }
      delay(pollIntervalMs)
    }
  }

  actual fun release() {
    scope.cancel()
  }
}

@Composable
public actual fun rememberCameraDeviceState(): State<CameraDeviceState> {
  val manager = remember { CameraDevicesManager() }
  DisposableEffect(manager) { onDispose { manager.release() } }
  return manager.cameraDevicesState.collectAsState()
}
