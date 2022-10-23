package br.com.devlucasyuji.sample.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.devlucasyuji.camposer.CameraPreview
import br.com.devlucasyuji.camposer.state.CamSelector
import br.com.devlucasyuji.camposer.state.CaptureMode
import br.com.devlucasyuji.camposer.state.FlashMode
import br.com.devlucasyuji.camposer.state.rememberCameraSelector
import br.com.devlucasyuji.camposer.state.rememberCameraState
import br.com.devlucasyuji.camposer.state.rememberFlashMode
import br.com.devlucasyuji.sample.extensions.noClickable
import br.com.devlucasyuji.sample.feature.camera.components.ActionBox
import br.com.devlucasyuji.sample.feature.camera.components.SettingsBox
import br.com.devlucasyuji.sample.feature.camera.components.VideoBox

@Composable
fun CamposerScreen(viewModel: CamposerViewModel = viewModel()) {
    val cameraState = rememberCameraState()
    var flashMode by cameraState.rememberFlashMode()
    var camSelector by rememberCameraSelector(CamSelector.Back)
    var zoomRatio by remember { mutableStateOf(cameraState.minZoom) }
    var zoomHasChanged by remember { mutableStateOf(false) }
    val hasFlashUnit by rememberUpdatedState(cameraState.hasFlashUnit)
    var captureMode by remember { mutableStateOf(CaptureMode.Image) }
    val isRecording by rememberUpdatedState(cameraState.isRecording)

    // FIXME add lifecycle aware
    val uiState by viewModel.uiState.collectAsState()

    CameraPreview(
        cameraState = cameraState,
        camSelector = camSelector,
        captureMode = captureMode,
        flashMode = flashMode,
        zoomRatio = zoomRatio,
        onZoomRatioChanged = {
            zoomHasChanged = true
            zoomRatio = it
        }
    ) {
        when (uiState) {
            UiState.CaptureSuccess -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
            }

            UiState.Initial -> Unit
        }
        CameraSection(
            Modifier.fillMaxSize(),
            zoomHasChanged = zoomHasChanged,
            zoomRatio = zoomRatio,
            flashMode = flashMode,
            isRecording = isRecording,
            captureMode = captureMode,
            hasFlashUnit = hasFlashUnit,
            onFlashModeChanged = { flashMode = it },
            onZoomFinish = { zoomHasChanged = false },
            onTakePicture = {
                cameraState.takePicture(
                    viewModel.imageContentValues,
                    onResult = viewModel::onImageResult
                )
            },
            onRecording = {
                cameraState.toggleRecording(
                    viewModel.videoContentValues,
                    onResult = viewModel::onVideoResult
                )
            },
            onSwitchCamera = {
                if (cameraState.isStreaming) {
                    camSelector = camSelector.reverse
                }
            },
            onCaptureModeChanged = { captureMode = it }
        )
    }
}

@Composable
fun CameraSection(
    modifier: Modifier = Modifier,
    zoomHasChanged: Boolean,
    zoomRatio: Float,
    flashMode: FlashMode,
    isRecording: Boolean,
    captureMode: CaptureMode,
    hasFlashUnit: Boolean,
    onFlashModeChanged: (FlashMode) -> Unit,
    onZoomFinish: () -> Unit,
    onRecording: () -> Unit,
    onTakePicture: () -> Unit,
    onSwitchCamera: () -> Unit,
    onCaptureModeChanged: (CaptureMode) -> Unit
) {
    VideoBox(
        modifier = Modifier.padding(top = 8.dp),
        isRecording = isRecording,
    )
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        SettingsBox(
            modifier = Modifier
                .fillMaxWidth()
                .noClickable()
                .padding(top = 32.dp, bottom = 16.dp, start = 24.dp, end = 24.dp),
            flashMode = flashMode,
            zoomRatio = zoomRatio,
            hasFlashUnit = hasFlashUnit,
            zoomHasChanged = zoomHasChanged,
            onFlashModeChanged = onFlashModeChanged,
            onZoomFinish = onZoomFinish,
        )
        ActionBox(
            modifier = Modifier
                .fillMaxWidth()
                .noClickable()
                .padding(bottom = 32.dp, top = 16.dp),
            captureMode = captureMode,
            onTakePicture = onTakePicture,
            onRecording = onRecording,
            onSwitchCamera = onSwitchCamera,
            onOptionChanged = onCaptureModeChanged,
        )
    }
}
