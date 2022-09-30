package br.com.devlucasyuji.camposer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import br.com.devlucasyuji.camposer.state.CamSelector
import br.com.devlucasyuji.camposer.state.FlashMode
import br.com.devlucasyuji.camposer.state.rememberCameraSelector
import br.com.devlucasyuji.camposer.state.rememberCameraState
import br.com.devlucasyuji.camposer.state.rememberCurrentZoom
import br.com.devlucasyuji.camposer.state.rememberFlashMode
import br.com.devlucasyuji.camposer.state.rememberImageAnalyzer
import br.com.devlucasyuji.camposer.state.rememberTorch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Camera()
        }
    }
}


@Composable
fun Camera() {
    val context = LocalContext.current
    val cameraState = rememberCameraState()

    var cameraSelector by rememberCameraSelector(customBackCamSelector())
    var flashMode by cameraState.rememberFlashMode(FlashMode.Off)
    var enableTorch by cameraState.rememberTorch(false)
    val currentZoom by cameraState.rememberCurrentZoom()

    val hasFlashUnit by rememberUpdatedState(cameraState.hasFlashUnit)

    var isPinchToZoomEnabled by remember { mutableStateOf(true) }
    var isFocusOnTapEnabled by remember { mutableStateOf(true) }
    var imageAnalyzer = cameraState.rememberImageAnalyzer() {

    }

    CameraPreview(
        Modifier.fillMaxSize(),
        cameraState = cameraState,
        flashMode = flashMode,
        imageAnalyzer = imageAnalyzer,
        isFocusOnTapEnabled = isFocusOnTapEnabled,
        isPinchToZoomEnabled = isPinchToZoomEnabled,
        enableTorch = enableTorch,
        camSelector = cameraSelector,
    ) {
        Box(Modifier.fillMaxSize()) {
            CameraDebugMode(
                zoom = currentZoom,
                flashMode = flashMode,
                camSelector = cameraSelector,
                isPinchToZoomEnabled = isPinchToZoomEnabled,
                isFocusOnTapEnabled = isFocusOnTapEnabled,
                enableTorch = enableTorch,
                hasFlashUnit = hasFlashUnit
            )

            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                RoundButton(
                    modifier = RoundedModifier
                        .clickable {
                            cameraState.takePicture {
                                Toast
                                    .makeText(context, "Result: $it", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                ) { Text("Pic") }
                val isStreaming by rememberUpdatedState(newValue = cameraState.isStreaming)
                RoundButton(
                    modifier = RoundedModifier.clickable {
                        if (isStreaming) {
                            cameraSelector = cameraSelector.customReverse
                        }
                    }
                ) { Text("Switch") }

                RoundButton(
                    modifier = RoundedModifier.clickable {
                        isPinchToZoomEnabled = !isPinchToZoomEnabled
                    }
                ) {
                    Text("Pinch")
                }

                RoundButton(
                    modifier = RoundedModifier.clickable {
                        isFocusOnTapEnabled = !isFocusOnTapEnabled
                    }
                ) {
                    Text("FocusTap")
                }

                RoundButton(
                    modifier = RoundedModifier.clickable { enableTorch = !enableTorch }
                ) {
                    Text("Torch ")
                }

                RoundButton(
                    modifier = RoundedModifier.clickable { flashMode = flashMode.inverse }
                ) {
                    Text("Flash")
                }
            }
        }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val isZoomPinchToZoom by rememberUpdatedState(cameraState.isPinchZoomInProgress)
        if (isZoomPinchToZoom) {
            Text("$currentZoom")
        }
    }
}

private val RoundedModifier
    get() = Modifier
        .padding(vertical = 32.dp, horizontal = 8.dp)
        .background(Color.Gray, CircleShape)
        .size(64.dp)


@Composable
fun CameraDebugMode(
    zoom: Float,
    flashMode: FlashMode,
    camSelector: CamSelector,
    enableTorch: Boolean,
    isPinchToZoomEnabled: Boolean,
    isFocusOnTapEnabled: Boolean,
    hasFlashUnit: Boolean
) {
    Column(
        Modifier.background(Color.Black.copy(alpha = 0.5F))
    ) {
        ProvideTextStyle(value = TextStyle(color = Color.White)) {
            Text("Camera Debug Mode:")
            Text("Zoom: $zoom")
            Text("Flash mode: $flashMode")
            Text("Camera selector: $camSelector")
            Text("Torch: $enableTorch")
            Text("Pinch to zoom: $isPinchToZoomEnabled")
            Text("Tap on focus: $isFocusOnTapEnabled")
            Text("Has flash: $hasFlashUnit")
        }
    }
}

@Composable
fun RoundButton(modifier: Modifier = Modifier, content: @Composable () -> Unit = {}) {
    Box(
        Modifier
            .then(modifier)
            .border(BorderStroke(4.dp, Color.LightGray), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
