package com.ujizin.camposer.shared.features.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ujizin.camposer.shared.features.camera.AspectRatioOption
import com.ujizin.camposer.state.properties.MirrorMode
import com.ujizin.camposer.state.properties.OrientationStrategy

@Composable
fun CameraSettingsOverlay(
  modifier: Modifier = Modifier,
  isVideoStabilizationSupported: Boolean,
  isTapToFocusSupported: Boolean,
  isVideoStabilizationEnabled: Boolean,
  is60FpsEnabled: Boolean,
  isTapToFocusEnabled: Boolean,
  aspectRatioOption: AspectRatioOption,
  orientationStrategy: OrientationStrategy,
  mirrorMode: MirrorMode,
  onVideoStabilizationChanged: (Boolean) -> Unit,
  on60FpsChanged: (Boolean) -> Unit,
  onTapToFocusChanged: (Boolean) -> Unit,
  onAspectRatioChanged: (AspectRatioOption) -> Unit,
  onOrientationStrategyChanged: (OrientationStrategy) -> Unit,
  onMirrorModeChanged: (MirrorMode) -> Unit,
  onClose: () -> Unit,
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.55f)),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .clip(RoundedCornerShape(20.dp)),
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
      tonalElevation = 8.dp,
    ) {
      Column(
        modifier = Modifier
          .verticalScroll(rememberScrollState())
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Text(
          text = "Camera Settings",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.SemiBold,
        )

        if (isVideoStabilizationSupported) {
          ToggleSetting(
            title = "Video Stabilization",
            valueLabel = if (isVideoStabilizationEnabled) "On" else "Off",
            checked = isVideoStabilizationEnabled,
            onCheckedChange = onVideoStabilizationChanged,
          )
          DisclaimerText(
            text = "Video stabilization will be applied only if supported.",
          )
        }

        ToggleSetting(
          title = "60 FPS",
          valueLabel = if (is60FpsEnabled) "60" else "30",
          checked = is60FpsEnabled,
          onCheckedChange = on60FpsChanged,
        )
        DisclaimerText(
          text = "60 FPS will be applied only if supported.",
        )

        if (isTapToFocusSupported) {
          ToggleSetting(
            title = "Tap to Focus",
            valueLabel = if (isTapToFocusEnabled) "On" else "Off",
            checked = isTapToFocusEnabled,
            onCheckedChange = onTapToFocusChanged,
          )
        }

        RadioGroupSetting(
          title = "Aspect Ratio",
          options = AspectRatioOption.entries.map { it.label to it },
          selected = aspectRatioOption,
          onSelected = onAspectRatioChanged,
        )
        DisclaimerText(
          text = "Target aspect ratio will be applied only if supported.",
        )

        RadioGroupSetting(
          title = "Orientation Strategy",
          options = listOf(
            "Preview" to OrientationStrategy.Preview,
            "Device" to OrientationStrategy.Device,
          ),
          selected = orientationStrategy,
          onSelected = onOrientationStrategyChanged,
        )
        DisclaimerText(
          text = "Orientation strategy Preview is not supported on Android yet.",
        )

        RadioGroupSetting(
          title = "Mirror Mode",
          options = listOf(
            "Off" to MirrorMode.Off,
            "On" to MirrorMode.On,
            "OnlyInFront" to MirrorMode.OnlyInFront,
          ),
          selected = mirrorMode,
          onSelected = onMirrorModeChanged,
        )

        Button(
          modifier = Modifier.fillMaxWidth(),
          onClick = onClose,
        ) {
          Text("Close")
        }
      }
    }
  }
}

@Composable
private fun ToggleSetting(
  title: String,
  valueLabel: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = valueLabel,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
    )
  }
}

@Composable
private fun <T> RadioGroupSetting(
  title: String,
  options: List<Pair<String, T>>,
  selected: T,
  onSelected: (T) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(
      text = title,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
    )
    options.forEach { (label, value) ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onSelected(value) },
        verticalAlignment = Alignment.CenterVertically,
      ) {
        RadioButton(
          selected = selected == value,
          onClick = { onSelected(value) },
        )
        Text(
          text = label,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
  }
}

@Composable
private fun DisclaimerText(
  text: String,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(RoundedCornerShape(999.dp))
        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
    )
    Text(
      text = text,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
