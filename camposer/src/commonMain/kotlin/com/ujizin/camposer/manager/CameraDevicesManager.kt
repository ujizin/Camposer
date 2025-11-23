package com.ujizin.camposer.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow

public expect class CameraDevicesManager {

    public val cameraDevicesState: StateFlow<CameraDeviceState>

    public fun release()
}

@Composable
public expect fun rememberCameraDeviceState(): State<CameraDeviceState>