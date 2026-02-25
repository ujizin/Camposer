package com.ujizin.camposer.session

import androidx.compose.runtime.Composable
import com.ujizin.camposer.CameraPreviewImpl
import com.ujizin.camposer.controller.camera.CameraController

/**
 * Camera State from [CameraPreviewImpl] Composable.
 * */
@Composable
public expect fun rememberCameraSession(controller: CameraController): CameraSession
