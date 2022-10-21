package br.com.devlucasyuji.sample.ui.preview

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.devlucasyuji.camposer.CameraPreview
import br.com.devlucasyuji.camposer.state.CamSelector
import br.com.devlucasyuji.camposer.state.FlashMode
import br.com.devlucasyuji.camposer.state.rememberCameraSelector
import br.com.devlucasyuji.camposer.state.rememberCameraState
import br.com.devlucasyuji.camposer.state.rememberFlashMode
import br.com.devlucasyuji.sample.extensions.noClickable
import br.com.devlucasyuji.sample.ui.preview.components.ActionBox
import br.com.devlucasyuji.sample.ui.preview.components.SettingsBox
import br.com.devlucasyuji.sample.ui.preview.model.Option

@Composable
fun CamposerScreen(viewModel: CamposerViewModel = viewModel()) {
    val cameraState = rememberCameraState()
    var flashMode by cameraState.rememberFlashMode()
    var camSelector by rememberCameraSelector(CamSelector.Back)
    var zoomRatio by remember { mutableStateOf(cameraState.minZoom) }
    var zoomHasChanged by remember { mutableStateOf(false) }
    // FIXME add lifecycle aware
    val uiState by viewModel.uiState.collectAsState()

    CameraPreview(
        cameraState = cameraState,
        camSelector = camSelector,
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
            onFlashModeChanged = { flashMode = it },
            onZoomFinish = { zoomHasChanged = false },
            onTakePicture = {
                cameraState.takePicture(
                    viewModel.imageContentValues,
                    onResult = viewModel::onImageResult
                )
            },
            onSwitchCamera = {
                if (cameraState.isStreaming) {
                    camSelector = camSelector.reverse
                }
            },
            onOptionChanged = {

            }
        )
    }
}

@Composable
fun CameraSection(
    modifier: Modifier = Modifier,
    zoomHasChanged: Boolean,
    zoomRatio: Float,
    flashMode: FlashMode,
    onFlashModeChanged: (FlashMode) -> Unit,
    onZoomFinish: () -> Unit,
    onTakePicture: () -> Unit,
    onSwitchCamera: () -> Unit,
    onOptionChanged: (Option) -> Unit
) {
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
            zoomHasChanged = zoomHasChanged,
            onFlashModeChanged = onFlashModeChanged,
            onZoomFinish = onZoomFinish
        )
        ActionBox(
            modifier = Modifier
                .fillMaxWidth()
                .noClickable()
                .padding(bottom = 32.dp, top = 16.dp),
            onTakePicture = onTakePicture,
            onSwitchCamera = onSwitchCamera,
            onOptionChanged = onOptionChanged,
        )
    }
}
