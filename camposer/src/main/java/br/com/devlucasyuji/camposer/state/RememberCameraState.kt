package br.com.devlucasyuji.camposer.state

import androidx.camera.core.CameraSelector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
fun rememberCameraState(): CameraState {
    val lifecycleOwner = LocalLifecycleOwner.current
    return rememberSaveable(saver = CameraState.getSaver(lifecycleOwner)) {
        CameraState(lifecycleOwner, CameraStore())
    }
}

@Composable
fun rememberCameraSelector(
    selector: CamSelector = CamSelector.Back
): MutableState<CamSelector> = remember {
    mutableStateOf(selector)
}

@Composable
fun rememberCameraSelector(
    block: () -> CameraSelector
): MutableState<CamSelector> = remember {
    derivedStateOf(block)
    mutableStateOf(customCamSelector(block))
}

fun customCamSelector(
    block: () -> CameraSelector
): CamSelector = CamSelector.CustomSelector(block())

@Composable
fun CameraState.rememberFlashMode(flashMode: FlashMode): MutableState<FlashMode> {
    val hasFlashUnit by rememberUpdatedState(hasFlashUnit)
    return remember(hasFlashUnit) {
        ConditionalState(flashMode, FlashMode.Off) { hasFlashUnit }
    }
}

@Composable
fun CameraState.rememberTorch(initialTorch: Boolean): MutableState<Boolean> {
    val hasFlashUnit by rememberUpdatedState(hasFlashUnit)
    return remember(hasFlashUnit) {
        ConditionalState(initialTorch, false) { hasFlashUnit }
    }
}

@Composable
fun CameraState.rememberCurrentZoom(): State<Float> = rememberUpdatedState(currentZoom)