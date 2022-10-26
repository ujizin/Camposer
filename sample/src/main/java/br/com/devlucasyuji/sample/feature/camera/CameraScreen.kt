package br.com.devlucasyuji.sample.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.devlucasyuji.camposer.CameraPreview
import br.com.devlucasyuji.camposer.state.CamSelector
import br.com.devlucasyuji.camposer.state.CaptureMode
import br.com.devlucasyuji.camposer.state.rememberCameraSelector
import br.com.devlucasyuji.camposer.state.rememberCameraState
import br.com.devlucasyuji.camposer.state.rememberFlashMode
import br.com.devlucasyuji.camposer.state.rememberTorch
import br.com.devlucasyuji.sample.extensions.noClickable
import br.com.devlucasyuji.sample.feature.camera.components.ActionBox
import br.com.devlucasyuji.sample.feature.camera.components.SettingsBox
import br.com.devlucasyuji.sample.feature.camera.components.VideoBox
import br.com.devlucasyuji.sample.feature.camera.mapper.toFlash
import br.com.devlucasyuji.sample.feature.camera.mapper.toFlashMode
import br.com.devlucasyuji.sample.feature.camera.model.Flash
import java.io.File

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun CameraScreen(
    viewModel: CamposerViewModel = viewModel(),
    onGalleryClick: () -> Unit
) {
    val cameraState = rememberCameraState()
    var flashMode by cameraState.rememberFlashMode()
    var camSelector by rememberCameraSelector(CamSelector.Back)
    var zoomRatio by remember { mutableStateOf(cameraState.minZoom) }
    var zoomHasChanged by remember { mutableStateOf(false) }
    val hasFlashUnit by rememberUpdatedState(cameraState.hasFlashUnit)
    var captureMode by remember { mutableStateOf(CaptureMode.Image) }
    val isRecording by rememberUpdatedState(cameraState.isRecording)
    var enableTorch by cameraState.rememberTorch(initialTorch = false)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CameraPreview(
        cameraState = cameraState,
        camSelector = camSelector,
        captureMode = captureMode,
        enableTorch = enableTorch,
        flashMode = flashMode,
        zoomRatio = zoomRatio,
        onZoomRatioChanged = {
            zoomHasChanged = true
            zoomRatio = it
        }
    ) {
        when (val result: CameraUiState = uiState) {
            CameraUiState.CaptureSuccess -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
            }

            is CameraUiState.Ready -> CameraSection(
                Modifier.fillMaxSize(),
                zoomHasChanged = zoomHasChanged,
                zoomRatio = zoomRatio,
                flashMode = flashMode.toFlash(enableTorch),
                isRecording = isRecording,
                captureMode = captureMode,
                hasFlashUnit = hasFlashUnit,
                onFlashModeChanged = { flash ->
                    enableTorch = flash == Flash.Always
                    flashMode = flash.toFlashMode()
                },
                onZoomFinish = { zoomHasChanged = false },
                onGalleryClick = onGalleryClick,
                lastPicture = result.lastPicture,
                onTakePicture = {
                    viewModel.takePicture(cameraState)
                },
                onRecording = {
                    viewModel.toggleRecording(cameraState)
                },
                onSwitchCamera = {
                    if (cameraState.isStreaming) {
                        camSelector = camSelector.reverse
                    }
                },
                onCaptureModeChanged = { captureMode = it }
            )

            CameraUiState.Initial -> Unit
        }
    }
}

@Composable
fun CameraSection(
    modifier: Modifier = Modifier,
    zoomHasChanged: Boolean,
    zoomRatio: Float,
    flashMode: Flash,
    isRecording: Boolean,
    captureMode: CaptureMode,
    hasFlashUnit: Boolean,
    lastPicture: File?,
    onGalleryClick: () -> Unit,
    onFlashModeChanged: (Flash) -> Unit,
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
            isVideo = captureMode == CaptureMode.Video,
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
            lastPicture = lastPicture,
            onGalleryClick = onGalleryClick,
            captureMode = captureMode,
            onTakePicture = onTakePicture,
            onRecording = onRecording,
            onSwitchCamera = onSwitchCamera,
            onOptionChanged = onCaptureModeChanged,
        )
    }
}
