package br.com.devlucasyuji.camposer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
fun rememberCameraState(): CameraState {
    val context = LocalContext.current
    return rememberSaveable(saver = CameraState.getSaver(context)) {
        CameraState(context, CameraStore())
    }
}