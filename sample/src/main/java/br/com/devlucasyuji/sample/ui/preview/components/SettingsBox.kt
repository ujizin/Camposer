package br.com.devlucasyuji.sample.ui.preview.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import br.com.devlucasyuji.camposer.state.FlashMode
import br.com.devlucasyuji.sample.extensions.roundTo
import kotlinx.coroutines.delay

@Composable
fun SettingsBox(
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