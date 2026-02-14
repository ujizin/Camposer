package com.ujizin.camposer.shared.features.camera

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.codescanner.CodeType
import com.ujizin.camposer.codescanner.rememberCodeImageAnalyzer
import com.ujizin.camposer.session.rememberCameraSession
import com.ujizin.camposer.shared.features.camera.components.BottomActionBar
import com.ujizin.camposer.shared.features.camera.components.TopControlsBar
import com.ujizin.camposer.shared.features.camera.components.ZoomSelector
import com.ujizin.camposer.state.properties.ImplementationMode
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.format.config.FrameRateConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import com.ujizin.camposer.state.properties.format.config.VideoStabilizationConfig

@Composable
fun CameraScreen(
  cameraViewModel: CameraViewModel = viewModel { CameraViewModel() },
) {
  val uiState by cameraViewModel.uiState.collectAsState()
  val cameraController = cameraViewModel.cameraController
  val cameraSession = rememberCameraSession(cameraController)

  // Camera state from session
  val flashMode by rememberUpdatedState(cameraSession.state.flashMode)
  val zoomRatio by rememberUpdatedState(cameraSession.state.zoomRatio)
  val isRecording by rememberUpdatedState(cameraController.isRecording)

  val isFlashSupported by rememberUpdatedState(cameraSession.info.isFlashSupported)
  val minZoom by rememberUpdatedState(cameraSession.info.minZoom)
  val maxZoom by rememberUpdatedState(cameraSession.info.maxZoom)

  val codeImageAnalyzer = cameraSession.rememberCodeImageAnalyzer(
    codeTypes = listOf(CodeType.QRCode),
    onError = {},
    codeAnalyzerListener = cameraViewModel::onCodeAnalyzed,
  )

  val camFormat = remember {
    CamFormat(
      FrameRateConfig(30),
      ResolutionConfig.UltraHigh,
      VideoStabilizationConfig(VideoStabilizationMode.Standard),
    )
  }

  LaunchedEffect(Unit) {
    cameraViewModel.initializeOrientationStrategy()
  }

  CameraPreview(
    modifier = Modifier.fillMaxSize(),
    cameraSession = cameraSession,
    camFormat = camFormat,
    scaleType = ScaleType.FitCenter,
    camSelector = uiState.camSelector,
    captureMode = uiState.captureMode,
    implementationMode = ImplementationMode.Compatible,
    imageAnalyzer = codeImageAnalyzer,
    isImageAnalysisEnabled = true,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      TopControlsBar(
        modifier = Modifier.fillMaxWidth(),
        flashMode = flashMode,
        isFlashSupported = isFlashSupported,
        onSettingsClick = { /* TODO: Open settings */ },
        onFlashClick = cameraViewModel::cycleFlashMode,
      )

      Spacer(Modifier.weight(1F))

      ZoomSelector(
        modifier = Modifier
          .padding(bottom = 24.dp)
          .align(Alignment.CenterHorizontally),
        currentZoom = zoomRatio,
        minZoom = minZoom,
        maxZoom = maxZoom,
        onZoomSelected = cameraViewModel::setZoom,
      )

      BottomActionBar(
        modifier = Modifier.fillMaxWidth(),
        isRecording = uiState.isRecording,
        captureMode = uiState.captureMode,
        thumbnail = uiState.lastThumbnail,
        onGalleryClick = cameraViewModel::openGallery,
        onShutterClick = cameraViewModel::capture,
        onCameraSwitchClick = cameraViewModel::toggleCamSelector,
        onCaptureModeSelected = cameraViewModel::setCaptureMode,
      )
    }
  }
}
