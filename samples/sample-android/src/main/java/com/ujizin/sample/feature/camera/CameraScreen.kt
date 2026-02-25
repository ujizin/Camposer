package com.ujizin.sample.feature.camera

import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skydoves.cloudy.cloudy
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.lifecycle.compose.collectStateWithLifecycle
import com.ujizin.camposer.manager.CameraDeviceState
import com.ujizin.camposer.manager.rememberCameraDeviceState
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.session.rememberCameraSession
import com.ujizin.camposer.session.rememberImageAnalyzer
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.format.config.AspectRatioConfig
import com.ujizin.camposer.state.properties.format.config.FrameRateConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import com.ujizin.camposer.state.properties.format.config.VideoStabilizationConfig
import com.ujizin.camposer.state.properties.selector.CamLensType
import com.ujizin.camposer.state.properties.selector.CamSelector
import com.ujizin.camposer.state.properties.selector.Saver
import com.ujizin.camposer.state.properties.selector.inverse
import com.ujizin.sample.extensions.noClickable
import com.ujizin.sample.feature.camera.components.ActionBox
import com.ujizin.sample.feature.camera.components.BlinkPictureBox
import com.ujizin.sample.feature.camera.components.SettingsBox
import com.ujizin.sample.feature.camera.mapper.toFlash
import com.ujizin.sample.feature.camera.mapper.toFlashMode
import com.ujizin.sample.feature.camera.model.CameraOption
import com.ujizin.sample.feature.camera.model.Flash
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun CameraScreen(
  viewModel: CameraViewModel = koinViewModel(),
  onGalleryClick: () -> Unit,
  onConfigurationClick: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  when (val result: CameraUiState = uiState) {
    is CameraUiState.Ready -> {
      val cameraController = remember { CameraController() }
      val cameraSession = rememberCameraSession(cameraController)
      val isRecording by cameraController.isRecording.collectAsStateWithLifecycle()
      val context = LocalContext.current
      CameraSection(
        cameraSession = cameraSession,
        useFrontCamera = result.user.useCamFront,
        usePinchToZoom = result.user.usePinchToZoom,
        useTapToFocus = result.user.useTapToFocus,
        lastPicture = result.lastPicture,
        qrCodeText = result.qrCodeText,
        onGalleryClick = onGalleryClick,
        onConfigurationClick = onConfigurationClick,
        onRecording = {
          viewModel.toggleRecording(
            context.contentResolver,
            cameraController,
          )
        },
        onTakePicture = { viewModel.takePicture(cameraController) },
        isRecording = isRecording,
        onAnalyzeImage = viewModel::analyzeImage,
      )

      LaunchedEffect(result.throwable) {
        if (result.throwable != null) {
          Toast
            .makeText(
              context,
              result.throwable.message,
              Toast.LENGTH_SHORT,
            ).show()
        }
      }
    }

    CameraUiState.Initial -> {
      Unit
    }
  }
}

@Composable
fun CameraSection(
  cameraSession: CameraSession,
  isRecording: Boolean,
  useFrontCamera: Boolean,
  usePinchToZoom: Boolean,
  useTapToFocus: Boolean,
  qrCodeText: String?,
  lastPicture: File?,
  onTakePicture: () -> Unit,
  onRecording: () -> Unit,
  onGalleryClick: () -> Unit,
  onAnalyzeImage: (ImageProxy) -> Unit,
  onConfigurationClick: () -> Unit,
) {
  var camSelector by rememberSaveable(stateSaver = CamSelector.Saver) {
    mutableStateOf(
      CamSelector(
        camPosition =
          when {
            useFrontCamera -> CamSelector.Front.camPosition
            else -> CamSelector.Back.camPosition
          },
        camLensTypes = listOf(CamLensType.Wide),
      ),
    )
  }
  val zoomRatio by cameraSession.state.zoomRatio.collectAsStateWithLifecycle()
  var zoomHasChanged by remember { mutableStateOf(false) }
  val cameraInfoState by cameraSession.info.collectStateWithLifecycle()
  val hasFlashUnit = cameraInfoState.isFlashSupported
  var cameraOption by rememberSaveable { mutableStateOf(CameraOption.Photo) }
  val flashMode by cameraSession.state.flashMode.collectAsStateWithLifecycle()
  val enableTorch by cameraSession.state.isTorchEnabled.collectAsStateWithLifecycle()
  val imageAnalyzer = cameraSession.rememberImageAnalyzer(analyze = onAnalyzeImage)

  LaunchedEffect(zoomRatio) {
    zoomHasChanged = true
  }
  val camDeviceState by rememberCameraDeviceState()

  LaunchedEffect(camDeviceState) {
    val camDeviceState = camDeviceState
    if (camDeviceState is CameraDeviceState.Devices) {
      Log.d("YUJI", "devices: ${camDeviceState.cameraDevices}")
//      camSelector = CamSelector(camDeviceState.cameraDevices.first())
    }
  }

  CameraPreview(
    cameraSession = cameraSession,
    camSelector = camSelector,
    captureMode = cameraOption.toCaptureMode(),
    camFormat =
      remember {
        CamFormat(
          AspectRatioConfig(1F),
          ResolutionConfig.UltraHigh,
          FrameRateConfig(60),
          VideoStabilizationConfig(VideoStabilizationMode.Standard),
        )
      },
    scaleType = ScaleType.FitCenter,
    imageAnalyzer = imageAnalyzer,
    isImageAnalysisEnabled = cameraOption == CameraOption.QRCode,
    isPinchToZoomEnabled = usePinchToZoom,
    isFocusOnTapEnabled = useTapToFocus,
    switchCameraContent = { bitmap ->
      Image(
        modifier = Modifier.cloudy(radius = 20),
        bitmap = bitmap,
        contentDescription = null,
      )
    },
  ) {
    BlinkPictureBox(lastPicture, cameraOption == CameraOption.Video)
    CameraInnerContent(
      Modifier.fillMaxSize(),
      zoomHasChanged = zoomHasChanged,
      zoomRatio = zoomRatio,
      flashMode = flashMode.toFlash(enableTorch),
      isRecording = isRecording,
      cameraOption = cameraOption,
      hasFlashUnit = hasFlashUnit,
      qrCodeText = qrCodeText,
      isVideoSupported = true,
      onFlashModeChanged = { flash ->
        with(cameraSession.controller) {
          setTorchEnabled(flash == Flash.Always)
          setFlashMode(flash.toFlashMode())
        }
      },
      onZoomFinish = { zoomHasChanged = false },
      lastPicture = lastPicture,
      onTakePicture = onTakePicture,
      onRecording = onRecording,
      onSwitchCamera = {
        if (cameraSession.isStreaming) {
          camSelector = camSelector.inverse
        }
      },
      onCameraOptionChanged = { cameraOption = it },
      onGalleryClick = onGalleryClick,
      onConfigurationClick = onConfigurationClick,
    )
  }
}

@Composable
fun CameraInnerContent(
  modifier: Modifier = Modifier,
  zoomHasChanged: Boolean,
  zoomRatio: Float,
  flashMode: Flash,
  isRecording: Boolean,
  cameraOption: CameraOption,
  hasFlashUnit: Boolean,
  qrCodeText: String?,
  lastPicture: File?,
  isVideoSupported: Boolean,
  onGalleryClick: () -> Unit,
  onFlashModeChanged: (Flash) -> Unit,
  onZoomFinish: () -> Unit,
  onRecording: () -> Unit,
  onTakePicture: () -> Unit,
  onConfigurationClick: () -> Unit,
  onSwitchCamera: () -> Unit,
  onCameraOptionChanged: (CameraOption) -> Unit,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    SettingsBox(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(top = 8.dp, bottom = 8.dp, start = 24.dp, end = 24.dp),
      flashMode = flashMode,
      zoomRatio = zoomRatio,
      isVideo = cameraOption == CameraOption.Video,
      hasFlashUnit = hasFlashUnit,
      zoomHasChanged = zoomHasChanged,
      isRecording = isRecording,
      onFlashModeChanged = onFlashModeChanged,
      onConfigurationClick = onConfigurationClick,
      onZoomFinish = onZoomFinish,
    )
    ActionBox(
      modifier =
        Modifier
          .fillMaxWidth()
          .noClickable()
          .padding(bottom = 32.dp, top = 16.dp),
      lastPicture = lastPicture,
      onGalleryClick = onGalleryClick,
      cameraOption = cameraOption,
      qrCodeText = qrCodeText,
      onTakePicture = onTakePicture,
      isRecording = isRecording,
      isVideoSupported = isVideoSupported,
      onRecording = onRecording,
      onSwitchCamera = onSwitchCamera,
      onCameraOptionChanged = onCameraOptionChanged,
    )
  }
}
