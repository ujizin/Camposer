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
import androidx.compose.runtime.LaunchedEffect
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
import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.code_scanner.model.CodeType
import com.ujizin.camposer.code_scanner.rememberCodeImageAnalyzer
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.manager.CameraDeviceState
import com.ujizin.camposer.manager.rememberCameraDeviceState
import com.ujizin.camposer.session.rememberCameraSession
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.format.config.AspectRatioConfig
import com.ujizin.camposer.state.properties.format.config.FrameRateConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import com.ujizin.camposer.state.properties.format.config.VideoStabilizationConfig
import com.ujizin.camposer.state.properties.inverse
import com.ujizin.camposer.state.properties.selector.CamLensType
import com.ujizin.camposer.state.properties.selector.CamPosition
import com.ujizin.camposer.state.properties.selector.CamSelector
import com.ujizin.camposer.state.properties.selector.inverse
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

    // Camera state
    val flashMode by rememberUpdatedState(cameraSession.state.flashMode)
    val isTorchEnabled by rememberUpdatedState(cameraSession.state.isTorchEnabled)
    val exposureCompensation by rememberUpdatedState(cameraSession.state.exposureCompensation)

    // Maybe passing to state?
    val isPreviewing by rememberUpdatedState(cameraSession.isStreaming)

    var camSelector by remember {
        mutableStateOf(
            CamSelector(
                camPosition = CamPosition.Back,
                camLensTypes = listOf(CamLensType.UltraWide),
            )
        )
    }
    val zoomRatio by rememberUpdatedState(cameraSession.state.zoomRatio)
    var captureMode by remember { mutableStateOf(CaptureMode.Image) }
    val isRecording by rememberUpdatedState(cameraController.isRecording)
    var videoPath by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val codeImageAnalyzer = cameraSession.rememberCodeImageAnalyzer(
        codeTypes = listOf(CodeType.Barcode39),
        onError = {}
    ) {
        text = "${it.type}: ${it.text}"
    }

    LaunchedEffect(Unit) {
        cameraController.setOrientationStrategy(OrientationStrategy.Preview)
    }

    val camDeviceState by rememberCameraDeviceState()

    LaunchedEffect(camDeviceState) {
        val camDeviceState = camDeviceState
        if (camDeviceState is CameraDeviceState.Devices) {
            println("Camera screen: ${camDeviceState.devices}")
            val cameraDevice = camDeviceState.devices.find {
                it.lensType.containsAll(
                    listOf(
                        CamLensType.UltraWide,
                        CamLensType.Wide,
                        CamLensType.Telephoto
                    )
                )
            } ?: camDeviceState.devices.first()
            camSelector = CamSelector(cameraDevice)
        }
    }

    CameraPreview(
        modifier = Modifier.fillMaxSize(),
        cameraSession = cameraSession,
        camFormat = remember {
            CamFormat(
                AspectRatioConfig(4F / 3f),
                FrameRateConfig(60),
                ResolutionConfig.UltraHigh,
                VideoStabilizationConfig(VideoStabilizationMode.Standard),
            )
        },
        scaleType = ScaleType.FitCenter,
        camSelector = camSelector,
        captureMode = captureMode,
        imageAnalyzer = codeImageAnalyzer,
    ) {
        FlowRow {
            val stabilization by rememberUpdatedState(cameraSession.state.videoStabilizationMode)
            Button(onClick = {}) {
                val fps by rememberUpdatedState(cameraSession.state.frameRate)
                Text("FPS: $fps")
            }
            Button(onClick = {
                val mode = when (stabilization) {
                    VideoStabilizationMode.Off -> VideoStabilizationMode.Standard
                    else -> VideoStabilizationMode.Off
                }
                val result = cameraController.setVideoStabilizationEnabled(
                    mode = mode
                )
                println("Mode to be set: $mode, result: ${result.isSuccess}, ${result.exceptionOrNull()}")
            }) {
                Text("Stabilization: $stabilization")
            }
            Button(onClick = {}) {
                Text("Is previewing: $isPreviewing")
            }
            if (isRecording) {
                Box(Modifier.size(24.dp).background(Color.Red, CircleShape))
            }
            Button(onClick = { cameraController.setTorchEnabled(!isTorchEnabled) }) {
                Text("Torch: $isTorchEnabled")
            }
            Button(onClick = { cameraController.setFlashMode(flashMode.inverse) }) {
                Text("Flash mode: $flashMode")
            }
            Button(onClick = {
                camSelector = camSelector.inverse
            }) {
                Text("Cam selector: $camSelector")
            }
            Button(onClick = { cameraController.setZoomRatio(zoomRatio + 1) }) {
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

                    CaptureMode.Video -> cameraController.startRecording(path.toString()) {
                        if (it is CaptureResult.Success) {
                            videoPath = it.data
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
                    cameraController.setExposureCompensation(exposureCompensation + 1F)
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
