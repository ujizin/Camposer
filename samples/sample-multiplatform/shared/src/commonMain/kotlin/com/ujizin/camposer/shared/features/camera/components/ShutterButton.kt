package com.ujizin.camposer.shared.features.camera.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Camera shutter button with recording animation.
 *
 * - Photo mode: purple ring + white inner circle
 * - Video recording: border fades out, inner circle shrinks to a small
 *   purple circle centered inside the white ring area
 */
@Composable
fun ShutterButton(
  modifier: Modifier = Modifier,
  size: Dp = 72.dp,
  isRecording: Boolean = false,
  onClick: () -> Unit = {},
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.9f else 1f,
    animationSpec = tween(durationMillis = 100),
    label = "shutterScale",
  )

  val borderColor by animateColorAsState(
    targetValue = if (isRecording) Color.Transparent else MaterialTheme.colorScheme.primary,
    animationSpec = tween(durationMillis = 300),
    label = "borderColor",
  )

  val innerSize by animateDpAsState(
    targetValue = if (isRecording) 24.dp else (size - 20.dp),
    animationSpec = tween(durationMillis = 300),
    label = "innerSize",
  )

  Box(
    modifier = modifier
      .size(size)
      .scale(scale)
      .clip(CircleShape)
      .border(
        width = 4.dp,
        color = borderColor,
        shape = CircleShape,
      )
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
      ),
    contentAlignment = Alignment.Center,
  ) {
    // White background ring (always visible)
    Box(
      modifier = Modifier
        .size(size - 12.dp)
        .clip(CircleShape)
        .background(Color.White),
      contentAlignment = Alignment.Center,
    ) {
      // Purple inner circle that shrinks when recording
      Box(
        modifier = Modifier
          .size(innerSize)
          .clip(CircleShape)
          .background(if (isRecording) MaterialTheme.colorScheme.primary else Color.White),
      )
    }
  }
}

@Preview
@Composable
private fun ShutterButtonIdlePreview() {
  ShutterButton(
    isRecording = false,
  )
}

@Preview
@Composable
private fun ShutterButtonRecordingPreview() {
  ShutterButton(
    isRecording = true,
  )
}
