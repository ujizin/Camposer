package br.com.devlucasyuji.sample.feature.camera

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.devlucasyuji.camposer.CameraPreview
import br.com.devlucasyuji.camposer.state.*
import br.com.devlucasyuji.sample.extensions.noClickable
import br.com.devlucasyuji.sample.feature.camera.components.ActionBox
import br.com.devlucasyuji.sample.feature.camera.components.BlinkPictureBox
import br.com.devlucasyuji.sample.feature.camera.components.SettingsBox
import br.com.devlucasyuji.sample.feature.camera.components.VideoBox
import br.com.devlucasyuji.sample.feature.camera.mapper.toFlash
import br.com.devlucasyuji.sample.feature.camera.mapper.toFlashMode
import br.com.devlucasyuji.sample.feature.camera.model.Flash
import org.koin.androidx.compose.get
import java.io.File

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = get(),
    onGalleryClick: () -> Unit,
    onConfigurationClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val result: CameraUiState = uiState) {
        is CameraUiState.Ready -> {
            val cameraState = rememberCameraState()
            CameraSection(
                cameraState = cameraState,
                useFrontCamera = result.user.useCamFront,
                usePinchToZoom = result.user.usePinchToZoom,
                useTapToFocus = result.user.useTapToFocus,
                lastPicture = result.lastPicture,
                onGalleryClick = onGalleryClick,
                onConfigurationClick = onConfigurationClick,
                onRecording = { viewModel.toggleRecording(cameraState) },
                onTakePicture = { viewModel.takePicture(cameraState) },
            )

            val context = LocalContext.current
            LaunchedEffect(result.throwable) {
                if (result.throwable != null) {
                    Toast.makeText(context, result.throwable.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        CameraUiState.Initial -> Unit
    }
}

@Composable
fun CameraSection(
    cameraState: CameraState,
    useFrontCamera: Boolean,
    usePinchToZoom: Boolean,
    useTapToFocus: Boolean,
    lastPicture: File?,
    onTakePicture: () -> Unit,
    onRecording: () -> Unit,
    onGalleryClick: () -> Unit,
    onConfigurationClick: () -> Unit,
) {
    var flashMode by cameraState.rememberFlashMode()
    var camSelector by rememberCameraSelector(if (useFrontCamera) CamSelector.Front else CamSelector.Back)
    var zoomRatio by remember { mutableStateOf(cameraState.minZoom) }
    var zoomHasChanged by remember { mutableStateOf(false) }
    val hasFlashUnit by rememberUpdatedState(cameraState.hasFlashUnit)
    var captureMode by remember { mutableStateOf(CaptureMode.Image) }
    val isRecording by rememberUpdatedState(cameraState.isRecording)
    var enableTorch by cameraState.rememberTorch(initialTorch = false)

    CameraPreview(
        cameraState = cameraState,
        camSelector = camSelector,
        captureMode = captureMode,
        enableTorch = enableTorch,
        flashMode = flashMode,
        zoomRatio = zoomRatio,
        isPinchToZoomEnabled = usePinchToZoom,
        isFocusOnTapEnabled = useTapToFocus,
        onZoomRatioChanged = {
            zoomHasChanged = true
            zoomRatio = it
        }
    ) {
        BlinkPictureBox(lastPicture)
        CameraInnerContent(
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
            lastPicture = lastPicture,
            onTakePicture = onTakePicture,
            onRecording = onRecording,
            onSwitchCamera = {
                if (cameraState.isStreaming) {
                    camSelector = camSelector.reverse
                }
            },
            onCaptureModeChanged = { captureMode = it },
            onGalleryClick = onGalleryClick,
            onConfigurationClick = onConfigurationClick
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
    captureMode: CaptureMode,
    hasFlashUnit: Boolean,
    lastPicture: File?,
    onGalleryClick: () -> Unit,
    onFlashModeChanged: (Flash) -> Unit,
    onZoomFinish: () -> Unit,
    onRecording: () -> Unit,
    onTakePicture: () -> Unit,
    onConfigurationClick: () -> Unit,
    onSwitchCamera: () -> Unit,
    onCaptureModeChanged: (CaptureMode) -> Unit,
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
                .padding(top = 16.dp, bottom = 16.dp, start = 24.dp, end = 24.dp),
            flashMode = flashMode,
            zoomRatio = zoomRatio,
            isVideo = captureMode == CaptureMode.Video,
            hasFlashUnit = hasFlashUnit,
            zoomHasChanged = zoomHasChanged,
            onFlashModeChanged = onFlashModeChanged,
            onConfigurationClick = onConfigurationClick,
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
