package com.ujizin.camposer.shared.features.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujizin.camposer.state.properties.FlashMode
import org.jetbrains.compose.resources.painterResource
import sample_android.sample_multiplatform.shared.generated.resources.Res
import sample_android.sample_multiplatform.shared.generated.resources.flash_auto
import sample_android.sample_multiplatform.shared.generated.resources.flash_off
import sample_android.sample_multiplatform.shared.generated.resources.flash_on
import sample_android.sample_multiplatform.shared.generated.resources.settings

/**
 * Top controls bar with settings, flash, and resolution indicator.
 *
 * @param modifier Modifier to be applied to the bar
 * @param flashMode Current flash mode
 * @param onSettingsClick Callback when settings button is clicked
 * @param onFlashClick Callback when flash button is clicked
 */
@Composable
fun TopControlsBar(
  modifier: Modifier = Modifier,
  flashMode: FlashMode = FlashMode.Auto,
  isFlashSupported: Boolean = true,
  isRecording: Boolean = false,
  recordingDurationSeconds: Long = 0L,
  onSettingsClick: () -> Unit = {},
  onFlashClick: () -> Unit = {},
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        ControlButton(
          icon = painterResource(Res.drawable.settings),
          contentDescription = "Settings",
          onClick = onSettingsClick,
        )
        if (isFlashSupported) {
          FlashButton(
            flashMode = flashMode,
            onClick = onFlashClick,
          )
        }
      }

      ResolutionIndicator()
    }

    if (isRecording) {
      RecordingDurationBadge(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 6.dp),
        durationInSeconds = recordingDurationSeconds,
      )
    }
  }
}

/**
 * Circular control button with icon.
 */
@Composable
fun ControlButton(
  icon: Painter,
  contentDescription: String,
  modifier: Modifier = Modifier,
  iconColor: Color = Color.White,
  onClick: () -> Unit = {},
) {
  Box(
    modifier = modifier
      .size(44.dp)
      .clip(CircleShape)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      painter = icon,
      contentDescription = contentDescription,
      tint = iconColor,
      modifier = Modifier.size(24.dp),
    )
  }
}

/**
 * Flash button that shows appropriate icon based on flash mode.
 */
@Composable
fun FlashButton(
  flashMode: FlashMode,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  val (icon, description) = when (flashMode) {
    FlashMode.Off -> painterResource(Res.drawable.flash_off) to "Flash Off"
    FlashMode.On -> painterResource(Res.drawable.flash_on) to "Flash On"
    FlashMode.Auto -> painterResource(Res.drawable.flash_auto) to "Flash Auto"
    else -> painterResource(Res.drawable.flash_off) to "Flash Off"
  }


  ControlButton(
    modifier = modifier,
    icon = icon,
    contentDescription = description,
    onClick = onClick,
  )
}

@Composable
private fun RecordingDurationBadge(
  durationInSeconds: Long,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
      .padding(horizontal = 24.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = durationInSeconds.toMinuteSecondFormat(),
      color = MaterialTheme.colorScheme.primary,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp,
    )
  }
}

/**
 * Resolution and aspect ratio indicator.
 */
@Composable
fun ResolutionIndicator(
  modifier: Modifier = Modifier,
  aspectRatio: String = "4:3",
) {
  Box(
    modifier = modifier
      .clip(MaterialTheme.shapes.medium)
      .padding(horizontal = 12.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = aspectRatio,
      color = Color.White,
      fontSize = 12.sp,
    )
  }
}

private fun Long.toMinuteSecondFormat(): String {
  val minutes = this / 60
  val seconds = this % 60
  return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

@Preview
@Composable
private fun TopControlsBarPreview() {
  TopControlsBar(
    modifier = Modifier.background(Color.Black),
    flashMode = FlashMode.Auto,
    isFlashSupported = true,
  )
}

@Preview
@Composable
private fun TopControlsBarFlashOffPreview() {
  TopControlsBar(
    modifier = Modifier.background(Color.Black),
    flashMode = FlashMode.Off,
    isFlashSupported = true,
  )
}

@Preview
@Composable
private fun TopControlsBarFlashOnPreview() {
  TopControlsBar(
    modifier = Modifier.background(Color.Black),
    flashMode = FlashMode.On,
    isFlashSupported = true,
  )
}

@Preview
@Composable
private fun ControlButtonPreview() {
  ControlButton(
    icon = painterResource(Res.drawable.settings),
    contentDescription = "Settings",
  )
}

@Preview
@Composable
private fun FlashButtonAutoPreview() {
  FlashButton(
    flashMode = FlashMode.Auto,
  )
}

@Preview
@Composable
private fun ResolutionIndicatorPreview() {
  ResolutionIndicator(
    modifier = Modifier.background(Color.Black),
    aspectRatio = "4:3",
  )
}
