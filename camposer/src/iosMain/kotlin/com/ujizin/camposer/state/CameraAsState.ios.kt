package com.ujizin.camposer.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
public actual fun rememberCameraState(): CameraState {
    return remember { CameraState() }
}
