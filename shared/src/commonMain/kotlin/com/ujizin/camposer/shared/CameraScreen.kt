package com.ujizin.camposer.shared

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.FlashMode
import com.ujizin.camposer.state.inverse
import com.ujizin.camposer.state.rememberCamSelector
import com.ujizin.camposer.state.rememberCameraState
import com.ujizin.camposer.state.rememberTorch

@Composable
fun CameraScreen() {
    val cameraState = rememberCameraState()
    var flashMode: FlashMode by remember { mutableStateOf(FlashMode.Off) }
    var enableTorch by cameraState.rememberTorch(false)
    var camSelector by rememberCamSelector(CamSelector.Back)
    var zoomRatio by remember { mutableStateOf(cameraState.minZoom) }

    CameraPreview(
        modifier = Modifier.fillMaxSize(),
        cameraState = cameraState,
        flashMode = flashMode,
        enableTorch = enableTorch,
        camSelector = camSelector,
        zoomRatio = zoomRatio,
    ) {
        FlowRow {
            Button(onClick = { enableTorch = !enableTorch }) {
                Text("Torch: $enableTorch")
            }
            Button(onClick = { flashMode = flashMode.inverse }) {
                Text("Flash mode: $flashMode")
            }
            Button(onClick = {
                camSelector = when (camSelector) {
                    CamSelector.Back -> CamSelector.Front
                    else -> CamSelector.Back
                }
            }) {
                Text("Cam selector: $camSelector")
            }
            Button(onClick = { zoomRatio += 1F }) {
                Text("zoom Ratio: $zoomRatio")
            }
        }
    }
}