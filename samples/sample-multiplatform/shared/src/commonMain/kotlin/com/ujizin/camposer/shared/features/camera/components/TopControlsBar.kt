package com.ujizin.camposer.shared.features.camera.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujizin.camposer.state.properties.FlashMode
import compose.icons.TablerIcons
import compose.icons.tablericons.BatteryAutomotive
import compose.icons.tablericons.Bolt
import compose.icons.tablericons.BoltOff
import compose.icons.tablericons.Settings

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
  onSettingsClick: () -> Unit = {},
  onFlashClick: () -> Unit = {},
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      ControlButton(
        icon = TablerIcons.Settings,
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
}

/**
 * Circular control button with icon.
 */
@Composable
fun ControlButton(
  icon: ImageVector,
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
      imageVector = icon,
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
    FlashMode.Off -> TablerIcons.BoltOff to "Flash Off"
    FlashMode.On -> TablerIcons.Bolt to "Flash On"
    FlashMode.Auto -> TablerIcons.BatteryAutomotive to "Flash Auto"
  }

  ControlButton(
    modifier = modifier,
    icon = icon,
    contentDescription = description,
    onClick = onClick,
  )
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
