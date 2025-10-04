package com.ujizin.camposer.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ujizin.camposer.controller.camera.CameraController

@Composable
public actual fun rememberCameraState(controller: CameraController): CameraState {
    return remember(controller) { CameraState(controller) }
}
