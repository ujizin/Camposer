package com.ujizin.camposer.shared.features.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.codescanner.CodeResult
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.shared.utils.getFirstFrameVideo
import com.ujizin.camposer.shared.utils.saveVideoToGallery
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import com.ujizin.camposer.state.properties.selector.inverse
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.saveImageToGallery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        savePictureToGallery(result.data)
        updateLastBitmapCaptured(result.data)
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
        val videoPath = result.data
        viewModelScope.launch {
          updateLastBitmapCaptured(getFirstFrameVideo(videoPath))
          FileKit.saveVideoToGallery(file = PlatformFile(videoPath))
        }
      }
    }
  }

  fun openGallery() {
    viewModelScope.launch {
      FileKit.openFilePicker(type = FileKitType.ImageAndVideo)
    }
  }

  fun capture() {
    when (_uiState.value.captureMode) {
      CaptureMode.Image -> takePicture()
      CaptureMode.Video -> toggleRecording()
      else -> Unit
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
      else -> FlashMode.Off
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

  @OptIn(ExperimentalUuidApi::class)
  private fun savePictureToGallery(
    pictureBytes: ByteArray,
  ) {
    viewModelScope.launch {
      FileKit.saveImageToGallery(
        filename = "camposer-${Uuid.random()}.jpeg",
        bytes = pictureBytes,
      )
    }
  }

  private fun updateLastBitmapCaptured(byteArray: ByteArray) {
    _uiState.update { state ->
      state.copy(lastThumbnail = byteArray)
    }
  }
}
