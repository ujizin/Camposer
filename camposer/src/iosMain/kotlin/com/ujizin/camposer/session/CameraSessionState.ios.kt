package com.ujizin.camposer.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.ujizin.camposer.controller.camera.CameraController

@Composable
public actual fun rememberCameraSession(controller: CameraController): CameraSession {
  val session = remember(controller) { CameraSession(controller) }
  DisposableEffect(Unit) { onDispose(session::dispose) }
  return session
}
