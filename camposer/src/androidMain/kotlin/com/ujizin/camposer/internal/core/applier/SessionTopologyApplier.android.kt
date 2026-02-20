package com.ujizin.camposer.internal.core.applier

import android.util.Range
import androidx.camera.view.CameraController.IMAGE_ANALYSIS
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.camerax.CameraXController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.mode
import com.ujizin.camposer.state.properties.selector.CamSelector
import com.ujizin.camposer.state.properties.value

internal class SessionTopologyApplier(
  private val cameraState: CameraState,
  private val cameraInfo: CameraInfo,
  private val cameraXController: CameraXController,
) : CameraStateApplier {
  override fun onCameraInitialized() {
    cameraXController.setEnabledUseCases(getUseCases())
    applyCamSelector(cameraState.camSelector.value)
  }

  fun applyCaptureMode(captureMode: CaptureMode) {
    cameraXController.setEnabledUseCases(
      getUseCases(
        mode = captureMode,
        isImageAnalyzerEnabled = cameraState.isImageAnalyzerEnabled.value,
      ),
    )
    resetPartialConfig()
    cameraState.updateCaptureMode(captureMode)
  }

  fun applyCamSelector(camSelector: CamSelector) {
    cameraXController.cameraSelector = camSelector.selector
    cameraInfo.rebind()
    resetConfig()
    cameraState.updateCamSelector(camSelector)
  }

  fun applyCamFormat(camFormat: CamFormat) {
    camFormat.applyConfigs(
      cameraInfo = cameraInfo,
      controller = cameraXController,
      onFrameRateChanged = {
        setFrameRate(it)
        cameraState.updateFrameRate(it)
      },
      onStabilizationModeChanged = {
        setVideoStabilizationMode(it)
        cameraState.updateVideoStabilizationMode(it)
      },
    )

    resetPartialConfig()
    cameraState.updateCamFormat(camFormat)
  }

  private fun setFrameRate(frameRate: Int) {
    cameraXController.videoCaptureTargetFrameRate = Range(frameRate, frameRate)
  }

  private fun resetPartialConfig() {
    resetExposureCompensation()
  }

  private fun resetConfig() {
    resetPartialConfig()
    resetFlashMode()
  }

  private fun getUseCases(
    mode: CaptureMode = cameraState.captureMode.value,
    isImageAnalyzerEnabled: Boolean = cameraState.isImageAnalyzerEnabled.value,
  ): Int =
    when {
      isImageAnalyzerEnabled && mode != CaptureMode.Video -> mode.value or IMAGE_ANALYSIS
      else -> mode.value
    }

  private fun resetExposureCompensation() {
    cameraXController.setExposureCompensationIndex(0)
    cameraState.updateExposureCompensation(0F)
  }

  private fun resetFlashMode() {
    cameraXController.imageCaptureFlashMode = FlashMode.Off.mode
    cameraState.updateFlashMode(FlashMode.Off)
  }

  private fun setVideoStabilizationMode(videoStabilizationMode: VideoStabilizationMode) {
    // TODO CameraX controller does not support yet :(
  }
}
