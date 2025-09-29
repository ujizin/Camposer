package com.ujizin.camposer.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.ujizin.camposer.CameraPreviewImpl
import com.ujizin.camposer.controller.CameraController

/**
 * Camera State from [CameraPreviewImpl] Composable.
 * */
@Composable
public expect fun rememberCameraState(
    controller: CameraController = remember { CameraController() },
): CameraState

/**
 * Camera selector's State to [CameraPreviewImpl] Composable.
 * */
@Composable
public fun rememberCamSelector(
    selector: CamSelector = CamSelector.Back
): MutableState<CamSelector> = rememberSaveable {
    mutableStateOf(selector)
}

/**
 * Flash mode's State to [CameraPreviewImpl] Composable.
 * */
@Composable
public fun CameraState.rememberFlashMode(
    initialFlashMode: FlashMode = FlashMode.Off,
    useSaver: Boolean = true
): MutableState<FlashMode> = rememberConditionalState(
    initialValue = initialFlashMode,
    defaultValue = FlashMode.Off,
    useSaver = useSaver,
    predicate = hasFlashUnit
)

/**
 * Torch's State to [CameraPreviewImpl] Composable.
 * */
@Composable
public fun CameraState.rememberTorch(
    initialTorch: Boolean = false,
    useSaver: Boolean = true
): MutableState<Boolean> = rememberConditionalState(
    initialValue = initialTorch,
    defaultValue = false,
    useSaver = useSaver,
    predicate = hasTorchAvailable
)