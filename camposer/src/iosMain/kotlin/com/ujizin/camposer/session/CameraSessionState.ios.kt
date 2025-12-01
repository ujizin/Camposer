package com.ujizin.camposer.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ujizin.camposer.controller.camera.CameraController

@Composable
public actual fun rememberCameraSession(controller: CameraController): CameraSession =
  remember(controller) { CameraSession(controller) }
