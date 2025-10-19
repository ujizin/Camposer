package com.ujizin.camposer.shared

import VideoPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.unit.dp
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.code_scanner.model.CodeType
import com.ujizin.camposer.code_scanner.rememberCodeImageAnalyzer
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.result.CaptureResult
import com.ujizin.camposer.state.properties.CamSelector
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.inverse
import com.ujizin.camposer.session.rememberCameraSession
import com.ujizin.camposer.session.rememberTorch
import kotlinx.io.files.Path
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun CameraScreen() {
    val cameraController = remember { CameraController() }
    val cameraSession = rememberCameraSession(cameraController)
    var flashMode: FlashMode by remember { mutableStateOf(FlashMode.Off) }
    var enableTorch by cameraSession.rememberTorch(false)
    var camSelector by remember { mutableStateOf(CamSelector.Back) }
    var zoomRatio by remember { mutableStateOf(cameraSession.info.minZoom) }
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var captureMode by remember { mutableStateOf(CaptureMode.Image) }
    val isRecording by rememberUpdatedState(cameraController.isRecording)
    var exposureCompensation by remember { mutableStateOf(0F) }
    var videoPath by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    val isPreviewing by rememberUpdatedState(cameraSession.isStreaming)
    val codeImageAnalyzer = cameraSession.rememberCodeImageAnalyzer(
        codeTypes = listOf(CodeType.Barcode39),
        onError = {}
    ) {
        text = "${it.type}: ${it.text}"
    }

    CameraPreview(
        modifier = Modifier.fillMaxSize(),
        cameraSession = cameraSession,
        flashMode = flashMode,
        isTorchEnabled = enableTorch,
        camSelector = camSelector,
        zoomRatio = zoomRatio,
        exposureCompensation = exposureCompensation,
        captureMode = captureMode,
        imageAnalyzer = codeImageAnalyzer,
        onZoomRatioChanged = { zoomRatio = it }
    ) {
        FlowRow {
            Button(onClick = {}) {
                Text("Is previewing: $isPreviewing")
            }
            if (isRecording) {
                Box(Modifier.size(24.dp).background(Color.Red, CircleShape))
            }
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
                Text("zoom Ratio: ${zoomRatio.roundDecimals(1)}")
            }
            Button(onClick = {
                val path = Path("$SystemTemporaryDirectory/video-${Uuid.random()}.mov")

                if (isRecording) {
                    cameraController.stopRecording()
                    return@Button
                }

                when (captureMode) {
                    CaptureMode.Image -> cameraController.takePicture {
                        if (it is CaptureResult.Success) {
                            bitmap = it.data.decodeToImageBitmap()
                        }
                    }

                    CaptureMode.Video -> cameraController.startRecording(path) {
                        if (it is CaptureResult.Success) {
                            videoPath = it.data.toString()
                        }
                    }

                    else -> {}
                }
            }) {
                Text("Take picture")
            }
            Button(onClick = {
                captureMode =
                    if (captureMode == CaptureMode.Image) CaptureMode.Video else CaptureMode.Image
            }) {
                Text("Capture mode: $captureMode")
            }
            Button(
                onClick = {
                    exposureCompensation = (exposureCompensation + 1).coerceAtMost(
                        cameraSession.info.maxExposure
                    )
                },
            ) {
                Text("exposureCompensation: $exposureCompensation")
            }

            Text(
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .padding(16.dp),
                text = text
            )
        }
    }

    bitmap?.let {
        Image(
            modifier = Modifier.fillMaxSize().clickable {
                bitmap = null
            },
            bitmap = it,
            contentDescription = null
        )
    }

    if (videoPath.isNotEmpty()) {
        VideoPlayer(
            modifier = Modifier.fillMaxSize(),
            url = "file://$videoPath",
            autoPlay = true,
            showControls = true,
        )
    }
}

fun Float.roundDecimals(n: Int = 1): Float {
    val factor = (10F * n).coerceAtLeast(1F)
    return (this * factor).roundToInt() / factor
}
