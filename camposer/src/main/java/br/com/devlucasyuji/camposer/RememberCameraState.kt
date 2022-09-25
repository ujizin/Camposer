package br.com.devlucasyuji.camposer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
fun rememberCameraState(): CameraState {
    val lifecycleOwner = LocalLifecycleOwner.current

    return rememberSaveable(saver = CameraState.getSaver(lifecycleOwner)) {
        CameraState(lifecycleOwner, CameraStore())
    }
}
