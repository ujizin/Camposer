package br.com.devlucasyuji.sample.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.devlucasyuji.camposer.CameraPreview
import br.com.devlucasyuji.sample.R
import br.com.devlucasyuji.sample.extensions.noClickable
import br.com.devlucasyuji.sample.extensions.roundTo
import br.com.devlucasyuji.camposer.state.CamSelector
import br.com.devlucasyuji.camposer.state.FlashMode
import br.com.devlucasyuji.camposer.state.rememberCameraSelector
import br.com.devlucasyuji.camposer.state.rememberCameraState
import br.com.devlucasyuji.camposer.state.rememberFlashMode
import kotlinx.coroutines.delay

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
        ActionsBox(
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

@Composable
private fun SettingsBox(
    modifier: Modifier = Modifier,
    zoomRatio: Float,
    zoomHasChanged: Boolean,
    flashMode: FlashMode,
    onFlashModeChanged: (FlashMode) -> Unit,
    onZoomFinish: () -> Unit,
) {
    Box(modifier = modifier) {
        FlashBox(
            modifier = Modifier.align(Alignment.TopStart),
            flashMode = flashMode,
            onFlashModeChanged = onFlashModeChanged
        )

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            visible = zoomHasChanged
        ) {
            Text(
                text = "${zoomRatio.roundTo(1)}X",
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                color = Color.White,
            )
        }
        Box(modifier = Modifier.align(Alignment.TopEnd))
    }
    LaunchedEffect(zoomRatio, zoomHasChanged) {
        delay(1_000)
        onZoomFinish()
    }
}

@Composable
private fun FlashBox(
    modifier: Modifier = Modifier,
    flashMode: FlashMode,
    onFlashModeChanged: (FlashMode) -> Unit
) {
    Button(
        modifier = Modifier
            .clip(CircleShape),
        onClick = { /*TODO*/ }) {
        Image(
            modifier = Modifier.size(32.dp),
            painter = painterResource(id = R.drawable.flash_off),
            contentDescription = ""
        )
    }
}

@Composable
private fun ActionsBox(
    modifier: Modifier = Modifier,
    onTakePicture: () -> Unit,
    onSwitchCamera: () -> Unit,
    onOptionChanged: (Option) -> Unit
) {
    Column(
        modifier = modifier,
    ) {
        OptionSection(
            modifier = Modifier.fillMaxWidth(),
            onOptionChanged = onOptionChanged
        )
        PictureActions(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 32.dp),
            onTakePicture = onTakePicture,
            onSwitchCamera = onSwitchCamera
        )
    }
}

enum class Option(val displayName: String) {
    Photo("Photo"), Video("Video")
}

@Composable
private fun OptionSection(
    modifier: Modifier = Modifier,
    currentOption: Option = Option.Photo,
    onOptionChanged: (Option) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
    ) {
        Option.values().forEach { option ->
            Text(
                modifier = Modifier.clickable { onOptionChanged(option) },
                text = option.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (currentOption == option) Color.Yellow else Color.White
            )
        }
    }
}

@Composable
private fun PictureActions(
    modifier: Modifier = Modifier,
    onTakePicture: () -> Unit,
    onSwitchCamera: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SwitchButton(onClick = onSwitchCamera)
        PictureButton(onClick = onTakePicture)
        SwitchButton(onClick = onSwitchCamera)
    }
}

@Composable
private fun SwitchButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var clicked by remember { mutableStateOf(false) }
    val rotate by animateFloatAsState(
        targetValue = if (clicked) 360F else 1F,
        animationSpec = tween(durationMillis = 500)
    )
    Button(
        modifier = Modifier
            .rotate(rotate)
            .size(48.dp)
            .background(Color.DarkGray.copy(alpha = 0.25F), CircleShape)
            .clip(CircleShape)
            .then(modifier),
        onClick = {
            clicked = !clicked
            onClick()
        }
    ) {
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.refresh),
            colorFilter = ColorFilter.tint(Color.White),
            contentDescription = "refresh"
        )
    }
}

@Composable
private fun PictureButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier
            .size(80.dp)
            .border(BorderStroke(4.dp, Color.White), CircleShape)
            .clip(CircleShape)
            .then(modifier),
        onClick = onClick
    )
}

@Composable
private fun Button(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9F else 1F)

    Box(
        modifier = Modifier
            .scale(scale)
            .then(modifier)
            .clickable(
                indication = rememberRipple(bounded = true),
                interactionSource = interactionSource,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}
