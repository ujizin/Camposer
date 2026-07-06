package com.ujizin.camposer.shared.features.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ujizin.camposer.permissions.PermissionStatus
import com.ujizin.camposer.permissions.isGranted
import com.ujizin.camposer.permissions.rememberAudioPermissionState
import com.ujizin.camposer.permissions.rememberCameraPermissionState

@Composable
fun PermissionsScreen(
  onAllPermissionGranted: () -> Unit,
) {
  val cameraPermission = rememberCameraPermissionState()
  val audioPermission = rememberAudioPermissionState()
  val isAllGranted = cameraPermission.status.isGranted && audioPermission.status.isGranted

  LaunchedEffect(isAllGranted) {
    if (isAllGranted) onAllPermissionGranted()
  }

  if (isAllGranted) return

  PermissionsContent(
    modifier = Modifier
      .fillMaxSize()
      .padding(20.dp),
    isCameraGranted = cameraPermission.status.isGranted,
    isRecordAudioGranted = audioPermission.status.isGranted,
    onCameraPermissionClick = {
      val status = cameraPermission.status
      if (status is PermissionStatus.Denied && !status.canRequestAgain) {
        cameraPermission.openAppSettings()
      } else {
        cameraPermission.launchPermissionRequest()
      }
    },
    onRecordAudioPermissionClick = {
      val status = audioPermission.status
      if (status is PermissionStatus.Denied && !status.canRequestAgain) {
        audioPermission.openAppSettings()
      } else {
        audioPermission.launchPermissionRequest()
      }
    },
  )
}

@Composable
private fun PermissionsContent(
  modifier: Modifier,
  isCameraGranted: Boolean,
  isRecordAudioGranted: Boolean,
  onRecordAudioPermissionClick: () -> Unit,
  onCameraPermissionClick: () -> Unit,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    PermissionButton(
      modifier = Modifier.fillMaxWidth(),
      text = "Camera Granted: $isCameraGranted",
      onClick = onCameraPermissionClick
    )
    PermissionButton(
      modifier = Modifier.fillMaxWidth(),
      text = "Record Audio Granted: $isRecordAudioGranted",
      onClick = onRecordAudioPermissionClick
    )
  }
}

@Preview(showSystemUi = true)
@Composable
private fun PermissionsContentPreview() {
  PermissionsContent(
    modifier = Modifier.fillMaxSize()
      .padding(20.dp),
    isCameraGranted = false,
    isRecordAudioGranted = false,
    onRecordAudioPermissionClick = {},
    onCameraPermissionClick = {},
  )
}

@Composable
private fun PermissionButton(
  text: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Button(
    modifier = modifier,
    onClick = onClick
  ) {
    Text(text)
  }
}