package com.ujizin.camposer.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.ujizin.camposer.CameraPreviewImpl
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.rememberConditionalState
import com.ujizin.camposer.controller.camera.CameraController

/**
 * Camera State from [CameraPreviewImpl] Composable.
 * */
@Composable
public expect fun rememberCameraSession(
    controller: CameraController,
): CameraSession

/**
 * Flash mode's State to [CameraPreviewImpl] Composable.
 * */
@Composable
public fun CameraSession.rememberFlashMode(
    initialFlashMode: FlashMode = FlashMode.Off,
    useSaver: Boolean = true,
): MutableState<FlashMode> = rememberConditionalState(
    initialValue = initialFlashMode,
    defaultValue = FlashMode.Off,
    useSaver = useSaver,
    predicate = info.isFlashSupported && info.isFlashAvailable
)

/**
 * Torch's State to [CameraPreviewImpl] Composable.
 * */
@Composable
public fun CameraSession.rememberTorch(
    initialTorch: Boolean = false,
    useSaver: Boolean = true,
): MutableState<Boolean> = rememberConditionalState(
    initialValue = initialTorch,
    defaultValue = false,
    useSaver = useSaver,
    predicate = info.isTorchSupported && info.isTorchAvailable
)