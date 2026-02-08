package com.ujizin.camposer.shared.features.camera.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

private val ZoomPresets = listOf(1.0f, 3.0f, 5.0f)
private const val ZOOM_MATCH_THRESHOLD = 0.05f

/**
 * Samsung-style dynamic zoom selector.
 *
 * Shows fixed presets (1x, 3x, 5x) plus the current zoom level
 * if it doesn't match any preset. The current zoom is always shown selected.
 *
 * Example: if current zoom is 3.5x, shows: 1x | 3x | 3.5x | 5x
 * with 3.5x highlighted as selected.
 */
@Composable
fun ZoomSelector(
  modifier: Modifier = Modifier,
  currentZoom: Float = 1.0f,
  minZoom: Float = 1.0f,
  maxZoom: Float = 1.0f,
  onZoomSelected: (Float) -> Unit = {},
) {
  val zoomLevels by remember(currentZoom, minZoom, maxZoom) {
    derivedStateOf {
      buildZoomLevels(currentZoom, minZoom, maxZoom)
    }
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(24.dp))
      .background(Color.Black.copy(alpha = 0.4f))
      .padding(4.dp),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(2.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      zoomLevels.forEach { level ->
        ZoomChip(
          label = level.label,
          isSelected = level.isSelected,
          onClick = { onZoomSelected(level.value) },
        )
      }
    }
  }
}

private data class ZoomLevel(
  val value: Float,
  val label: String,
  val isSelected: Boolean,
)

/**
 * Builds the list of zoom levels to display.
 * Only shows presets that are within the camera's [minZoom]..[maxZoom] range.
 * Inserts the current zoom between presets if it doesn't match any preset.
 */
private fun buildZoomLevels(
  currentZoom: Float,
  minZoom: Float,
  maxZoom: Float,
): List<ZoomLevel> {
  val availablePresets = ZoomPresets.filter { it in minZoom..maxZoom }

  val matchesPreset = availablePresets.any { abs(it - currentZoom) < ZOOM_MATCH_THRESHOLD }

  if (matchesPreset) {
    return availablePresets.map { preset ->
      ZoomLevel(
        value = preset,
        label = formatZoom(preset),
        isSelected = abs(preset - currentZoom) < ZOOM_MATCH_THRESHOLD,
      )
    }
  }

  val levels = mutableListOf<ZoomLevel>()
  var inserted = false

  for (preset in availablePresets) {
    if (!inserted && currentZoom < preset) {
      levels.add(
        ZoomLevel(
          value = currentZoom,
          label = formatZoom(currentZoom),
          isSelected = true,
        )
      )
      inserted = true
    }
    levels.add(
      ZoomLevel(
        value = preset,
        label = formatZoom(preset),
        isSelected = false,
      )
    )
  }

  if (!inserted) {
    levels.add(
      ZoomLevel(
        value = currentZoom,
        label = formatZoom(currentZoom),
        isSelected = true,
      )
    )
  }

  return levels
}

/**
 * Formats a zoom value for display.
 * Shows integer values without decimal (e.g. "3x"),
 * and fractional values with one decimal (e.g. "3.5x").
 */
private fun formatZoom(zoom: Float): String {
  val rounded = (zoom * 10).roundToInt() / 10f
  return if (rounded == rounded.toInt().toFloat()) {
    "${rounded.toInt()}x"
  } else {
    "${rounded}x"
  }
}

@Composable
private fun ZoomChip(
  label: String,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  val backgroundColor by animateColorAsState(
    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
    animationSpec = tween(durationMillis = 200),
    label = "chipBackground",
  )
  val textColor by animateColorAsState(
    targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
    animationSpec = tween(durationMillis = 200),
    label = "chipText",
  )

  Box(
    modifier = modifier
      .size(40.dp)
      .clip(CircleShape)
      .background(backgroundColor)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
      ),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = label,
      color = textColor,
      fontSize = 12.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
    )
  }
}
