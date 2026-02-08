package com.ujizin.camposer.shared.features.camera

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

  fun initializeOrientationStrategy() {
    cameraController.setOrientationStrategy(OrientationStrategy.Preview)
  }

  fun takePicture() {
    cameraController.takePicture { result ->
      if (result is CaptureResult.Success) {
        val bitmap = result.data
        _uiState.update { state ->
          state.copy(
            capturedBitmap = bitmap,
            lastThumbnail = bitmap
          )
        }
      }
    }
  }

  @OptIn(ExperimentalUuidApi::class)
  fun toggleRecording() {
    if (cameraController.isRecording) {
      cameraController.stopRecording()
      _uiState.update { it.copy(isRecording = false) }
      return
    }

    val path = Path("$SystemTemporaryDirectory/video-${Uuid.random()}.mov")
    _uiState.update { it.copy(isRecording = true) }
    cameraController.startRecording(path.toString()) { result ->
      _uiState.update { it.copy(isRecording = false) }
      if (result is CaptureResult.Success) {
        _uiState.update { state ->
          state.copy(videoPath = result.data)
        }
      }
    }
  }

  fun capture() {
    when (_uiState.value.captureMode) {
      CaptureMode.Image -> takePicture()
      CaptureMode.Video -> toggleRecording()
    }
  }

  fun toggleCamSelector() {
    _uiState.update { state ->
      state.copy(camSelector = state.camSelector.inverse)
    }
  }

  fun setCaptureMode(mode: CaptureMode) {
    _uiState.update { state ->
      state.copy(captureMode = mode)
    }
  }

  /**
   * Set zoom to a specific ratio.
   */
  fun setZoom(zoom: Float) {
    cameraController.setZoomRatio(zoom)
  }

  /**
   * Cycle flash mode: Off -> Auto -> On -> Off
   */
  fun cycleFlashMode() {
    val flashMode = cameraController.state?.flashMode ?: return
    val nextMode = when (flashMode) {
      FlashMode.Off -> FlashMode.Auto
      FlashMode.Auto -> FlashMode.On
      FlashMode.On -> FlashMode.Off
    }
    cameraController.setFlashMode(nextMode)
  }

  fun onCodeAnalyzed(code: CodeResult) {
    _uiState.update { state ->
      state.copy(
        codeScanText = "${code.type}: ${code.text}",
        frameRect = code.frameRect,
        corners = code.corners,
      )
    }
  }

}
