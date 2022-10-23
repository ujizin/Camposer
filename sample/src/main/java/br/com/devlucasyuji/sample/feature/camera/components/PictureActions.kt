package br.com.devlucasyuji.sample.feature.camera.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.devlucasyuji.camposer.state.CaptureMode
import br.com.devlucasyuji.sample.R


@Composable
fun PictureActions(
    modifier: Modifier = Modifier,
    captureMode: CaptureMode,
    onRecording: () -> Unit,
    onTakePicture: () -> Unit,
    onSwitchCamera: () -> Unit,
) {
    val isVideo by remember(captureMode) { derivedStateOf { captureMode == CaptureMode.Video } }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SwitchButton(onClick = onSwitchCamera)
        PictureButton(isVideo = isVideo, onClick = {
            if (isVideo) onRecording() else onTakePicture()
        })
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
            contentDescription = stringResource(R.string.refresh)
        )
    }
}

@Composable
private fun PictureButton(
    modifier: Modifier = Modifier,
    isVideo: Boolean,
    onClick: () -> Unit,
) {
    val color by animateColorAsState(
        targetValue = if (isVideo) Color.Red else Color.Transparent,
        animationSpec = tween(durationMillis = 250)
    )
    Button(
        modifier = Modifier
            .size(80.dp)
            .border(BorderStroke(4.dp, Color.White), CircleShape)
            .clip(CircleShape)
            .padding(12.dp)
            .background(color, CircleShape)
            .then(modifier),
        onClick = onClick
    )
}
