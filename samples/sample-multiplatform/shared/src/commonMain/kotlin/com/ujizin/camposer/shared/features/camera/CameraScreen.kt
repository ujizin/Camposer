package com.ujizin.camposer.shared.features.camera

import VideoPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.codescanner.CodeType
import com.ujizin.camposer.codescanner.rememberCodeImageAnalyzer
import com.ujizin.camposer.manager.CameraDeviceState
import com.ujizin.camposer.manager.rememberCameraDeviceState
import com.ujizin.camposer.session.rememberCameraSession
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.format.config.FrameRateConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import com.ujizin.camposer.state.properties.format.config.VideoStabilizationConfig
import com.ujizin.camposer.state.properties.selector.CamLensType
import kotlin.math.roundToInt

@Composable
fun CameraScreen(
  cameraViewModel: CameraViewModel = viewModel { CameraViewModel() },
) {
  val uiState by cameraViewModel.uiState.collectAsState()
  val cameraController = cameraViewModel.cameraController
  val cameraSession = rememberCameraSession(cameraController)

  // Camera state from session
  val flashMode by rememberUpdatedState(cameraSession.state.flashMode)
  val isTorchEnabled by rememberUpdatedState(cameraSession.state.isTorchEnabled)
  val exposureCompensation by rememberUpdatedState(cameraSession.state.exposureCompensation)
  val isPreviewing by rememberUpdatedState(cameraSession.isStreaming)
  val zoomRatio by rememberUpdatedState(cameraSession.state.zoomRatio)
  val isRecording by rememberUpdatedState(cameraController.isRecording)

  val codeImageAnalyzer =
    cameraSession.rememberCodeImageAnalyzer(
      codeTypes = listOf(CodeType.QRCode),
      onError = {},
      codeAnalyzerListener = cameraViewModel::onCodeAnalyzed,
    )


  LaunchedEffect(Unit) {
    cameraViewModel.initializeOrientationStrategy()
  }

  val camDeviceState by rememberCameraDeviceState()

  LaunchedEffect(camDeviceState) {
    val deviceState = camDeviceState
    if (deviceState is CameraDeviceState.Devices) {
      println("Camera screen: ${deviceState.cameraDevices}")
      val cameraDevice =
        deviceState.cameraDevices.find {
          it.lensType.containsAll(
            listOf(
              CamLensType.UltraWide,
              CamLensType.Wide,
              CamLensType.Telephoto,
            ),
          )
        } ?: deviceState.cameraDevices.first()
      // camSelector = CamSelector(cameraDevice)
    }
  }

  CameraPreview(
    modifier = Modifier.fillMaxSize(),
    cameraSession = cameraSession,
    camFormat =
      remember {
        CamFormat(
          FrameRateConfig(30),
          ResolutionConfig.UltraHigh,
          VideoStabilizationConfig(VideoStabilizationMode.Standard),
        )
      },
    scaleType = ScaleType.FitCenter,
    camSelector = uiState.camSelector,
    captureMode = uiState.captureMode,
    imageAnalyzer = codeImageAnalyzer,
    isImageAnalysisEnabled = true,
  ) {
    FlowRow {
      val stabilization by rememberUpdatedState(cameraSession.state.videoStabilizationMode)
      Button(onClick = {}) {
        val fps by rememberUpdatedState(cameraSession.state.frameRate)
        Text("FPS: $fps")
      }
      Button(onClick = { cameraViewModel.toggleVideoStabilization() }) {
        Text("Stabilization: $stabilization")
      }
      Button(onClick = {}) {
        Text("Is previewing: $isPreviewing")
      }
      if (isRecording) {
        Box(Modifier.size(24.dp).background(Color.Red, CircleShape))
      }
      Button(onClick = { cameraViewModel.toggleTorch() }) {
        Text("Torch: $isTorchEnabled")
      }
      Button(onClick = { cameraViewModel.toggleFlashMode() }) {
        Text("Flash mode: $flashMode")
      }
      Button(onClick = { cameraViewModel.toggleCamSelector() }) {
        Text("Cam selector: ${uiState.camSelector}")
      }
      Button(onClick = { cameraViewModel.increaseZoom() }) {
        Text("zoom Ratio: ${zoomRatio.roundDecimals(1)}")
      }
      Button(onClick = { cameraViewModel.capture() }) {
        Text("Take picture")
      }
      Button(onClick = { cameraViewModel.toggleCaptureMode() }) {
        Text("Capture mode: ${uiState.captureMode}")
      }
      Button(onClick = { cameraViewModel.increaseExposure() }) {
        Text("exposureCompensation: $exposureCompensation")
      }

      Text(
        modifier =
          Modifier
            .align(Alignment.Bottom)
            .padding(16.dp),
        text = uiState.codeScanText,
      )
    }
  }

  uiState.capturedBitmap?.let { bitmap ->
    Image(
      modifier =
        Modifier.fillMaxSize().clickable {
          cameraViewModel.clearCapturedBitmap()
        },
      bitmap = bitmap,
      contentDescription = null,
    )
  }

  if (uiState.videoPath.isNotEmpty()) {
    VideoPlayer(
      modifier = Modifier.fillMaxSize(),
      url = "file://${uiState.videoPath}",
      autoPlay = true,
      showControls = true,
    )
  }

  uiState.frameRect?.let { rect ->
    Canvas(Modifier.fillMaxSize()) {
      val path =
        androidx.compose.ui.graphics
          .Path()

      uiState.corners.forEachIndexed { index, it ->
        if (index == 0) {
          path.moveTo(it.x.dp.toPx(), it.y.dp.toPx())
        }
        path.lineTo(it.x.dp.toPx(), it.y.dp.toPx())
      }

      drawPath(
        color = Color.Red,
        path = path,
      )
    }
  }
}

fun Float.roundDecimals(n: Int = 1): Float {
  val factor = (10F * n).coerceAtLeast(1F)
  return (this * factor).roundToInt() / factor
}
