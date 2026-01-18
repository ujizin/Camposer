package com.ujizin.camposer.shared.features.camera

import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.lifecycle.ViewModel
import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.codescanner.CodeResult
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import com.ujizin.camposer.state.properties.selector.inverse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.io.files.Path
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CameraViewModel : ViewModel() {

  val cameraController = CameraController()

  private val _uiState = MutableStateFlow(CameraUiState())
  val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

  /**
   * Initialize orientation strategy for the camera.
   */
  fun initializeOrientationStrategy() {
    cameraController.setOrientationStrategy(OrientationStrategy.Preview)
  }

  /**
   * Take a picture and update the UI state with the captured bitmap.
   */
  fun takePicture() {
    cameraController.takePicture { result ->
      if (result is CaptureResult.Success) {
        _uiState.update { state ->
          state.copy(capturedBitmap = result.data.decodeToImageBitmap())
        }
      }
    }
  }

  /**
   * Start or stop video recording based on current state.
   */
  @OptIn(ExperimentalUuidApi::class)
  fun toggleRecording() {
    if (cameraController.isRecording) {
      cameraController.stopRecording()
      return
    }

    val path = Path("$SystemTemporaryDirectory/video-${Uuid.random()}.mov")
    cameraController.startRecording(path.toString()) { result ->
      if (result is CaptureResult.Success) {
        _uiState.update { state ->
          state.copy(videoPath = result.data)
        }
      }
    }
  }

  /**
   * Capture based on the current capture mode (Image or Video).
   */
  fun capture() {
    when (_uiState.value.captureMode) {
      CaptureMode.Image -> takePicture()
      CaptureMode.Video -> toggleRecording()
    }
  }

  /**
   * Toggle between front and back camera.
   */
  fun toggleCamSelector() {
    _uiState.update { state ->
      state.copy(camSelector = state.camSelector.inverse)
    }
  }

  /**
   * Toggle between Image and Video capture modes.
   */
  fun toggleCaptureMode() {
    _uiState.update { state ->
      state.copy(
        captureMode = if (state.captureMode == CaptureMode.Image) {
          CaptureMode.Video
        } else {
          CaptureMode.Image
        }
      )
    }
  }

  /**
   * Increase zoom ratio by 1.
   */
  fun increaseZoom() {
    val currentZoom = cameraController.state?.zoomRatio ?: 1f
    cameraController.setZoomRatio(currentZoom + 1)
  }

  /**
   * Increase exposure compensation by 1.
   */
  fun increaseExposure() {
    val currentExposure = cameraController.state?.exposureCompensation ?: 0f
    cameraController.setExposureCompensation(currentExposure + 1f)
  }

  /**
   * Toggle flash mode between On and Off.
   */
  fun toggleFlashMode() {
    val currentFlash = cameraController.state?.flashMode ?: FlashMode.Off
    cameraController.setFlashMode(
      if (currentFlash == FlashMode.Off) FlashMode.On else FlashMode.Off
    )
  }

  /**
   * Toggle torch on/off.
   */
  fun toggleTorch() {
    val isTorchEnabled = cameraController.state?.isTorchEnabled ?: false
    cameraController.setTorchEnabled(!isTorchEnabled)
  }

  /**
   * Toggle video stabilization mode.
   */
  fun toggleVideoStabilization() {
    val currentMode = cameraController.state?.videoStabilizationMode
    val newMode = when (currentMode) {
      com.ujizin.camposer.state.properties.VideoStabilizationMode.Off ->
        com.ujizin.camposer.state.properties.VideoStabilizationMode.Standard
      else -> com.ujizin.camposer.state.properties.VideoStabilizationMode.Off
    }
    val result = cameraController.setVideoStabilizationEnabled(newMode)
    println("Mode to be set: $newMode, result: ${result.isSuccess}, ${result.exceptionOrNull()}")
  }

  /**
   * Handle code scan result from the image analyzer.
   * Can be used as method reference: viewModel::onCodeAnalyzed
   */
  fun onCodeAnalyzed(code: CodeResult) {
    _uiState.update { state ->
      state.copy(
        codeScanText = "${code.type}: ${code.text}",
        frameRect = code.frameRect,
        corners = code.corners,
      )
    }
  }

  /**
   * Clear the captured bitmap.
   */
  fun clearCapturedBitmap() {
    _uiState.update { state ->
      state.copy(capturedBitmap = null)
    }
  }

  /**
   * Clear the video path.
   */
  fun clearVideoPath() {
    _uiState.update { state ->
      state.copy(videoPath = "")
    }
  }
}