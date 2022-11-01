package br.com.devlucasyuji.sample.feature.camera

import android.widget.Toast
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.devlucasyuji.camposer.CameraPreview
import br.com.devlucasyuji.camposer.state.CamSelector
import br.com.devlucasyuji.camposer.state.CameraState
import br.com.devlucasyuji.camposer.state.rememberCameraSelector
import br.com.devlucasyuji.camposer.state.rememberCameraState
import br.com.devlucasyuji.camposer.state.rememberFlashMode
import br.com.devlucasyuji.camposer.state.rememberImageAnalyzer
import br.com.devlucasyuji.camposer.state.rememberTorch
import br.com.devlucasyuji.sample.extensions.noClickable
import br.com.devlucasyuji.sample.feature.camera.components.ActionBox
import br.com.devlucasyuji.sample.feature.camera.components.BlinkPictureBox
import br.com.devlucasyuji.sample.feature.camera.components.SettingsBox
import br.com.devlucasyuji.sample.feature.camera.mapper.toFlash
import br.com.devlucasyuji.sample.feature.camera.mapper.toFlashMode
import br.com.devlucasyuji.sample.feature.camera.model.CameraOption
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
                qrCodeText = result.qrCodeText,
                onGalleryClick = onGalleryClick,
                onConfigurationClick = onConfigurationClick,
                onRecording = { viewModel.toggleRecording(cameraState) },
                onTakePicture = { viewModel.takePicture(cameraState) },
                onAnalyzeImage = { viewModel.analyzeImage(it) }
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
    qrCodeText: String?,
    lastPicture: File?,
    onTakePicture: () -> Unit,
    onRecording: () -> Unit,
    onGalleryClick: () -> Unit,
    onAnalyzeImage: (ImageProxy) -> Unit,
    onConfigurationClick: () -> Unit,
) {
    var flashMode by cameraState.rememberFlashMode()
    var camSelector by rememberCameraSelector(if (useFrontCamera) CamSelector.Front else CamSelector.Back)
    var zoomRatio by rememberSaveable { mutableStateOf(cameraState.minZoom) }
    var zoomHasChanged by rememberSaveable { mutableStateOf(false) }
    val hasFlashUnit by rememberUpdatedState(cameraState.hasFlashUnit)
    var cameraOption by rememberSaveable { mutableStateOf(CameraOption.Photo) }
    val isRecording by rememberUpdatedState(cameraState.isRecording)
    var enableTorch by cameraState.rememberTorch(initialTorch = false)
    val imageAnalyzer = cameraState.rememberImageAnalyzer { onAnalyzeImage(it) }

    CameraPreview(
        cameraState = cameraState,
        camSelector = camSelector,
        captureMode = cameraOption.toCaptureMode(),
        enableTorch = enableTorch,
        flashMode = flashMode,
        zoomRatio = zoomRatio,
        imageAnalyzer = imageAnalyzer.takeIf { cameraOption == CameraOption.QRCode },
        isPinchToZoomEnabled = usePinchToZoom,
        isFocusOnTapEnabled = useTapToFocus,
        onZoomRatioChanged = {
            zoomHasChanged = true
            zoomRatio = it
        }
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
            onCameraOptionChanged = { cameraOption = it },
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
    cameraOption: CameraOption,
    hasFlashUnit: Boolean,
    qrCodeText: String?,
    lastPicture: File?,
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
            modifier = Modifier
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
            modifier = Modifier
                .fillMaxWidth()
                .noClickable()
                .padding(bottom = 32.dp, top = 16.dp),
            lastPicture = lastPicture,
            onGalleryClick = onGalleryClick,
            cameraOption = cameraOption,
            qrCodeText = qrCodeText,
            onTakePicture = onTakePicture,
            isRecording = isRecording,
            onRecording = onRecording,
            onSwitchCamera = onSwitchCamera,
            onCameraOptionChanged = onCameraOptionChanged,
        )
    }
}
