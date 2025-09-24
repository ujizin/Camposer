package com.ujizin.camposer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import com.ujizin.camposer.focus.FocusTap
import com.ujizin.camposer.focus.SquareCornerFocus
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.CaptureMode
import com.ujizin.camposer.state.FlashMode
import com.ujizin.camposer.state.ImageAnalyzer
import com.ujizin.camposer.state.ImageCaptureMode
import com.ujizin.camposer.state.ImageTargetSize
import com.ujizin.camposer.state.ImplementationMode
import com.ujizin.camposer.state.ResolutionPreset
import com.ujizin.camposer.state.ScaleType
import kotlinx.coroutines.delay

/**
 * Creates a Camera Preview's composable.
 *
 * @param cameraState camera state hold some states and camera's controller, it can be useful to given action like [CameraState.takePicture]
 * @param camSelector camera selector to be added, default is back
 * @param captureMode camera capture mode, default is image
 * @param imageCaptureMode camera image capture mode, default is minimum latency for better performance
 * @param imageCaptureTargetSize suggested target size for image camera capture, default is camera's preferred size
 * @param flashMode flash mode to be added, default is off
 * @param scaleType scale type to be added, default is fill center
 * @param enableTorch enable torch from camera, default is false.
 * @param exposureCompensation camera exposure compensation to be added
 * @param zoomRatio zoom ratio to be added, default is 1.0
 * @param imageAnalyzer image analyzer from camera, see [ImageAnalyzer]
 * @param implementationMode implementation mode to be added, default is performance
 * @param isImageAnalysisEnabled enable or disable image analysis
 * @param isFocusOnTapEnabled turn on feature focus on tap if true
 * @param isPinchToZoomEnabled turn on feature pinch to zoom if true
 * @param onPreviewStreamChanged dispatch when preview is switching to front or back
 * @param onSwitchToFront composable preview when change camera to front and it's not been streaming yet
 * @param onSwitchToBack composable preview when change camera to back and it's not been streaming yet
 * @param onZoomRatioChanged dispatch when zoom is changed by pinch to zoom
 * @param focusTapContent content of focus tap, default is [SquareCornerFocus]
 * @param onFocus callback to use when on focus tap is triggered, call onComplete to [focusTapContent] gone.
 * @param content content composable within of camera preview.
 * @see ImageAnalyzer
 * @see CameraState
 * */
@Composable
public fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraState: CameraState,
    camSelector: CamSelector = cameraState.camSelector,
    captureMode: CaptureMode = cameraState.captureMode,
    imageCaptureMode: ImageCaptureMode = cameraState.imageCaptureMode,
    imageCaptureTargetSize: ImageTargetSize? = cameraState.imageCaptureTargetSize,
    flashMode: FlashMode = cameraState.flashMode,
    scaleType: ScaleType = cameraState.scaleType,
    enableTorch: Boolean = cameraState.enableTorch,
    exposureCompensation: Float = cameraState.initialExposure,
    zoomRatio: Float = 1F,
    imageAnalyzer: ImageAnalyzer? = null,
    resolutionPreset: ResolutionPreset = cameraState.resolutionPreset,
    implementationMode: ImplementationMode = cameraState.implementationMode,
    isImageAnalysisEnabled: Boolean = cameraState.isImageAnalysisEnabled,
    isFocusOnTapEnabled: Boolean = cameraState.isFocusOnTapEnabled,
    isPinchToZoomEnabled: Boolean = cameraState.isZoomSupported,
    onPreviewStreamChanged: () -> Unit = {},
    onSwitchToFront: @Composable (ImageBitmap) -> Unit = {},
    onSwitchToBack: @Composable (ImageBitmap) -> Unit = {},
    onFocus: suspend (onComplete: () -> Unit) -> Unit = { onComplete ->
        delay(1000L)
        onComplete()
    },
    onZoomRatioChanged: (Float) -> Unit = {},
    focusTapContent: @Composable () -> Unit = { SquareCornerFocus() },
    content: @Composable () -> Unit = {},
) {
    val isCameraIdle by rememberUpdatedState(!cameraState.isStreaming)
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    var latestBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    CameraPreviewImpl(
        modifier = modifier,
        cameraState = cameraState,
        captureMode = captureMode,
        camSelector = camSelector,
        imageCaptureMode = imageCaptureMode,
        imageCaptureTargetSize = imageCaptureTargetSize,
        flashMode = flashMode,
        resolutionPreset = resolutionPreset,
        scaleType = scaleType,
        enableTorch = enableTorch,
        exposureCompensation = exposureCompensation,
        zoomRatio = zoomRatio,
        imageAnalyzer = imageAnalyzer,
        implementationMode = implementationMode,
        isImageAnalysisEnabled = isImageAnalysisEnabled,
        isFocusOnTapEnabled = isFocusOnTapEnabled,
        isPinchToZoomEnabled = isPinchToZoomEnabled,
        onZoomRatioChanged = onZoomRatioChanged,
        focusTapContent = focusTapContent,
        onTapFocus = { tapOffset = it },
        onSwitchCamera = { latestBitmap = it },
    )

    FocusTap(
        offset = tapOffset,
        onFocus = { onFocus { tapOffset = Offset.Zero } },
    ) { focusTapContent() }

    if (isCameraIdle) {
        latestBitmap?.let {
            when (camSelector) {
                CamSelector.Front -> onSwitchToFront(it)
                CamSelector.Back -> onSwitchToBack(it)
                else -> Unit
            }
            LaunchedEffect(latestBitmap) {
                onPreviewStreamChanged()
                if (latestBitmap != null) onZoomRatioChanged(cameraState.minZoom)
            }
        }
    }

    content()
}

@Composable
internal expect fun CameraPreviewImpl(
    modifier: Modifier = Modifier,
    cameraState: CameraState,
    camSelector: CamSelector,
    captureMode: CaptureMode,
    resolutionPreset: ResolutionPreset,
    imageCaptureMode: ImageCaptureMode,
    imageCaptureTargetSize: ImageTargetSize?,
    flashMode: FlashMode,
    scaleType: ScaleType,
    enableTorch: Boolean,
    exposureCompensation: Float?,
    zoomRatio: Float,
    imageAnalyzer: ImageAnalyzer?,
    implementationMode: ImplementationMode,
    isImageAnalysisEnabled: Boolean,
    isFocusOnTapEnabled: Boolean,
    isPinchToZoomEnabled: Boolean,
    onTapFocus: (Offset) -> Unit,
    onSwitchCamera: (ImageBitmap) -> Unit,
    onZoomRatioChanged: (Float) -> Unit = {},
    focusTapContent: @Composable () -> Unit = { SquareCornerFocus() },
    content: @Composable () -> Unit = {},
)