package com.ujizin.camposer.shared.features.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ujizin.camposer.state.properties.CaptureMode
import compose.icons.TablerIcons
import compose.icons.tablericons.CameraRotate

/**
 * Unified bottom section containing the capture mode selector and action buttons.
 *
 * Groups the capture mode tabs (PHOTO/VIDEO) together with the shutter,
 * gallery thumbnail, and camera switch button as a single cohesive component.
 */
@Composable
fun BottomActionBar(
  modifier: Modifier = Modifier,
  isRecording: Boolean = false,
  captureMode: CaptureMode = CaptureMode.Image,
  thumbnail: ByteArray? = null,
  onGalleryClick: () -> Unit = {},
  onShutterClick: () -> Unit = {},
  onCameraSwitchClick: () -> Unit = {},
  onCaptureModeSelected: (CaptureMode) -> Unit = {},
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    CaptureModeSelector(
      modifier = Modifier.fillMaxWidth(),
      selectedMode = captureMode,
      onModeSelected = onCaptureModeSelected,
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 32.dp)
        .padding(bottom = 48.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      GalleryThumbnail(
        thumbnail = thumbnail,
        onClick = onGalleryClick,
      )

      ShutterButton(
        isRecording = isRecording,
        onClick = onShutterClick,
      )

      CameraSwitchButton(
        onClick = onCameraSwitchClick,
      )
    }
  }
}

/**
 * Circular gallery thumbnail showing last captured image.
 */
@Composable
private fun GalleryThumbnail(
  thumbnail: ByteArray?,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  Box(
    modifier = modifier
      .size(52.dp)
      .clip(CircleShape)
      .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
      .background(Color.DarkGray)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    if (thumbnail != null) {
      AsyncImage(
        model = thumbnail,
        contentDescription = "Last captured image",
        modifier = Modifier.matchParentSize(),
        contentScale = ContentScale.Crop,
      )
    }
  }
}

/**
 * Camera switch button.
 */
@Composable
private fun CameraSwitchButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  Box(
    modifier = modifier
      .size(52.dp)
      .clip(CircleShape)
      .background(Color.Gray.copy(alpha = 0.6f))
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = TablerIcons.CameraRotate,
      contentDescription = "Switch camera",
      tint = Color.White,
      modifier = Modifier.size(28.dp),
    )
  }
}

@Preview
@Composable
private fun BottomActionBarPhotoModePreview() {
  BottomActionBar(
    modifier = Modifier.background(Color.Black),
    isRecording = false,
    captureMode = CaptureMode.Image,
  )
}

@Preview
@Composable
private fun BottomActionBarVideoRecordingPreview() {
  BottomActionBar(
    modifier = Modifier.background(Color.Black),
    isRecording = true,
    captureMode = CaptureMode.Video,
  )
}
