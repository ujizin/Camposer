package com.ujizin.camposer.shared.features.camera.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujizin.camposer.state.properties.CaptureMode

data class CaptureModeItem(
  val mode: CaptureMode,
  val label: String,
)

/**
 * iOS-style capture mode selector where the selected mode is centered
 * with a pill background, and unselected modes appear alongside.
 */
@Composable
fun CaptureModeSelector(
  modifier: Modifier = Modifier,
  selectedMode: CaptureMode = CaptureMode.Image,
  modes: List<CaptureModeItem> = listOf(
    CaptureModeItem(CaptureMode.Image, "PHOTO"),
    CaptureModeItem(CaptureMode.Video, "VIDEO"),
  ),
  onModeSelected: (CaptureMode) -> Unit = {},
) {
  Row(
    modifier = modifier.padding(vertical = 12.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    modes.forEach { item ->
      val isSelected = item.mode == selectedMode

      val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent,
        animationSpec = tween(durationMillis = 250),
        label = "modeBackground",
      )

      val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 250),
        label = "modeTextColor",
      )

      val horizontalPadding by animateDpAsState(
        targetValue = if (isSelected) 20.dp else 12.dp,
        animationSpec = tween(durationMillis = 250),
        label = "modePadding",
      )

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(backgroundColor)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onModeSelected(item.mode) },
          )
          .padding(horizontal = horizontalPadding, vertical = 8.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = item.label,
          color = textColor,
          fontSize = 13.sp,
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
          textAlign = TextAlign.Center,
          letterSpacing = 1.sp,
        )
      }
    }
  }
}

@Preview
@Composable
private fun CaptureModeSelectorPhotoPreview() {
  CaptureModeSelector(
    modifier = Modifier.background(Color.Black),
    selectedMode = CaptureMode.Image,
  )
}

@Preview
@Composable
private fun CaptureModeSelectorVideoPreview() {
  CaptureModeSelector(
    modifier = Modifier.background(Color.Black),
    selectedMode = CaptureMode.Video,
  )
}
