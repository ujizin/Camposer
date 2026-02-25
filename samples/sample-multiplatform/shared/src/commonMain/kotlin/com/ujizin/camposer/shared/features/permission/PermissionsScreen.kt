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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory

@Composable
fun PermissionsScreen(
  onAllPermissionGranted: () -> Unit,
) {
  val factory = rememberPermissionsControllerFactory()
  val controller: PermissionsController =
    remember(factory) { factory.createPermissionsController() }

  BindEffect(controller)

  val viewModel = viewModel {
    PermissionsViewModel(controller)
  }

  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(uiState.isAllPermissionGranted) {
    if (uiState.isAllPermissionGranted) {
      onAllPermissionGranted()
    }
  }

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME, onEvent = viewModel::onResume)

  if (uiState.isLoading || uiState.isAllPermissionGranted) {
    return
  }

  PermissionsContent(
    modifier = Modifier
      .fillMaxSize()
      .padding(20.dp),
    uiState = uiState,
    onRecordAudioPermissionClick = viewModel::provideRecordAudioPermission,
    onCameraPermissionClick = viewModel::provideCameraPermission,
  )
}

@Composable
fun PermissionsContent(
  modifier: Modifier,
  uiState: PermissionsUiState,
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
      text = "Camera Granted: ${uiState.isCameraGranted}",
      onClick = onCameraPermissionClick
    )
    PermissionButton(
      modifier = Modifier.fillMaxWidth(),
      text = "Record Audio Granted: ${uiState.isRecordAudioGranted}",
      onClick = onRecordAudioPermissionClick
    )
  }
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

@Preview(showSystemUi = true)
@Composable
private fun PermissionsContentPreview() {
  PermissionsContent(
    modifier = Modifier.fillMaxSize()
      .padding(20.dp),
    uiState = PermissionsUiState(
      isCameraGranted = false,
      isRecordAudioGranted = false
    ),
    onRecordAudioPermissionClick = {},
    onCameraPermissionClick = {},
  )
}