package com.ujizin.multiplatform_sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.rememberCameraState

@Composable
fun MultiplatformCameraScreen() {
    Permission {
        val cameraState = rememberCameraState()
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            cameraState = cameraState
        ) { }
    }
}

@Composable
expect fun Permission(content: @Composable () -> Unit)