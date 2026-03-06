package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.internal.capture.JvmCameraCapture
import com.ujizin.camposer.internal.utils.Logger
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.selector.CamSelector
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal actual class SessionTopologyApplier(
  private val cameraState: CameraState,
  private val capture: JvmCameraCapture,
) : CameraStateApplier {
  private val sessionTopologyMutex = Mutex()

  private var camSelectorJob: Job? = null

  fun applyCaptureMode(captureMode: CaptureMode) {
    cameraState.updateCaptureMode(captureMode)
  }

  fun applyCamSelector(camSelector: CamSelector) {
    camSelectorJob?.cancel()
    camSelectorJob = lockedLaunch {
      if (cameraState.camSelector.value == camSelector) return@lockedLaunch
      applyCamSelectorInternal(camSelector)
    }
  }

  private suspend fun applyCamSelectorInternal(camSelector: CamSelector) {
    capture.stopStreaming()
    capture.release()
    val success = capture.open(camSelector.deviceIndex)
    if (success) {
      capture.startStreaming()
      cameraState.updateCamSelector(camSelector)
    } else {
      Logger.error("Failed to switch camera to index ${camSelector.deviceIndex}", null)
    }
  }

  fun applyCamFormat(camFormat: CamFormat) {
    cameraState.updateCamFormat(camFormat)
  }

  private fun lockedLaunch(block: suspend () -> Unit): Job =
    cameraState.launch {
      sessionTopologyMutex.withLock {
        withContext(NonCancellable) { block() }
      }
    }
}
