package com.ujizin.camposer.internal.core.applier

import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.jvm.JvmCameraController
import com.ujizin.camposer.internal.utils.Logger
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.selector.CamSelector

internal actual class SessionTopologyApplier(
  private val cameraState: CameraState,
  private val cameraInfo: CameraInfo,
  private val jvmCameraController: JvmCameraController,
) : CameraStateApplier {
  override fun onCameraInitialized() {
    setCamFormat(cameraState.camFormat.value)
  }

  /** Capture mode changes no hardware on desktop. The microphone opens when recording starts. */
  actual fun applyCaptureMode(captureMode: CaptureMode) {
    cameraState.updateCaptureMode(captureMode)
  }

  actual fun applyCamSelector(camSelector: CamSelector) {
    if (jvmCameraController.deviceIndex == camSelector.deviceIndex) {
      cameraState.updateCamSelector(camSelector)
      return
    }

    jvmCameraController.open(camSelector.deviceIndex) { result ->
      result
        .onSuccess {
          setCamFormat(cameraState.camFormat.value)
          cameraState.updateCamSelector(camSelector)
        }.onFailure { error ->
          Logger.error("Failed to switch camera to ${camSelector.deviceIndex}", error)
        }
    }
  }

  // Runs off the caller thread: the hardware write waits for the capture lock.
  actual fun applyCamFormat(camFormat: CamFormat) {
    cameraState.launch {
      setCamFormat(camFormat)
      cameraState.updateCamFormat(camFormat)
    }
  }

  private fun setCamFormat(camFormat: CamFormat) {
    camFormat.applyConfigs(
      jvmCameraController = jvmCameraController,
      onFrameRateChanged = { frameRate ->
        jvmCameraController.setFrameRate(frameRate)
        val appliedFrameRate = jvmCameraController.currentFormat?.maxFps ?: frameRate
        cameraState.updateFrameRate(appliedFrameRate)
      },
      onStabilizationModeChanged = cameraState::updateVideoStabilizationMode,
    )
    cameraInfo.updateInfo()
  }
}
