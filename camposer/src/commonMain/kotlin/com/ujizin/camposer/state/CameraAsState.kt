package com.ujizin.camposer.state

import androidx.compose.runtime.Composable
import com.ujizin.camposer.CameraPreview

/**
 * Camera State from [CameraPreview] Composable.
 * */
@Composable
public expect fun rememberCameraState(): CameraState
