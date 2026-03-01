package com.ujizin.camposer.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.ujizin.camposer.state.properties.selector.CamLensType
import com.ujizin.camposer.state.properties.selector.CamPosition
import com.ujizin.camposer.state.properties.selector.CameraId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.bytedeco.javacv.OpenCVFrameGrabber

internal actual class CameraDevicesManager(
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
  private val pollIntervalMs: Long = 3_000L,
) {
  private val scope = CoroutineScope(dispatcher)
  private val _cameraDevicesState = MutableStateFlow<CameraDeviceState>(CameraDeviceState.Initial)
  actual val cameraDevicesState: StateFlow<CameraDeviceState> = _cameraDevicesState.asStateFlow()

  init {
    scope.launch { poll() }
  }

  private suspend fun poll() {
    while (true) {
      val discovered = discoverDevices()
      _cameraDevicesState.update { discovered }
      delay(pollIntervalMs)
    }
  }

  private fun discoverDevices(): CameraDeviceState {
    val devices = mutableListOf<CameraDevice>()
    for (index in 0..9) {
      val grabber = OpenCVFrameGrabber(index)
      try {
        grabber.start()
        devices += CameraDevice(
          cameraId = CameraId(index.toString()),
          name = "Camera $index",
          position = CamPosition.External,
          fov = 0f,
          lensType = listOf(CamLensType.Wide),
          photoData = emptyList(),
          videoData = emptyList(),
        )
        grabber.stop()
      } catch (_: Exception) {
        break
      }
    }
    return if (devices.isEmpty()) CameraDeviceState.Initial else CameraDeviceState.Devices(devices)
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
