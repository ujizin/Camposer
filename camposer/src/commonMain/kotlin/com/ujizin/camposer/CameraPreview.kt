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
import com.ujizin.camposer.config.properties.CamSelector
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.config.properties.CaptureMode
import com.ujizin.camposer.config.properties.FlashMode
import com.ujizin.camposer.config.properties.ImageAnalyzer
import com.ujizin.camposer.config.properties.ImageCaptureStrategy
import com.ujizin.camposer.config.properties.ImplementationMode
import com.ujizin.camposer.config.properties.ResolutionPreset
import com.ujizin.camposer.config.properties.ScaleType
import kotlinx.coroutines.delay

/**
 * Creates a Camera Preview's composable.
 *
 * @param cameraState camera state hold some states and camera's controller, it can be useful to given action like [CameraState.takePicture]
 * @param camSelector camera selector to be added, default is back
 * @param captureMode camera capture mode, default is image
 * @param captureStrategy camera image capture mode, default is minimum latency for better performance
 * @param flashMode flash mode to be added, default is off
 * @param scaleType scale type to be added, default is fill center
 * @param isTorchEnabled enable torch from camera, default is false.
 * @param exposureCompensation camera exposure compensation to be added
 * @param zoomRatio zoom ratio to be added, default is 1.0
 * @param imageAnalyzer image analyzer from camera, see [com.ujizin.camposer.config.properties.ImageAnalyzer]
 * @param implementationMode implementation mode to be added, default is performance
 * @param isImageAnalysisEnabled enable or disable image analysis
 * @param isFocusOnTapEnabled turn on feature focus on tap if true
 * @param isPinchToZoomEnabled turn on feature pinch to zoom if true
 * @param onPreviewStreamChanged dispatch when preview is switching to front or back
 * @param onSwitchCameraContent composable preview when change camera and it's not been streaming yet
 * @param onZoomRatioChanged dispatch when zoom is changed by pinch to zoom
 * @param focusTapContent content of focus tap, default is [SquareCornerFocus]
 * @param onFocus callback to use when on focus tap is triggered, call onComplete to [focusTapContent] gone.
 * @param content content composable within of camera preview.
 * @see com.ujizin.camposer.config.properties.ImageAnalyzer
 * @see CameraState
 * */
@Composable
public fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraState: CameraState,
    camSelector: CamSelector = cameraState.config.camSelector,
    captureMode: CaptureMode = cameraState.config.captureMode,
    captureStrategy: ImageCaptureStrategy = cameraState.config.imageCaptureStrategy,
    flashMode: FlashMode = cameraState.config.flashMode,
    scaleType: ScaleType = cameraState.config.scaleType,
    isTorchEnabled: Boolean = cameraState.config.isTorchEnabled,
    exposureCompensation: Float? = null,
    zoomRatio: Float = 1F,
    imageAnalyzer: ImageAnalyzer? = null,
    resolutionPreset: ResolutionPreset = cameraState.config.resolutionPreset,
    implementationMode: ImplementationMode = cameraState.config.implementationMode,
    isImageAnalysisEnabled: Boolean = imageAnalyzer != null,
    isFocusOnTapEnabled: Boolean = cameraState.config.isFocusOnTapEnabled,
    isPinchToZoomEnabled: Boolean = cameraState.config.isPinchToZoomEnabled,
    onPreviewStreamChanged: () -> Unit = {},
    onSwitchCameraContent: @Composable (ImageBitmap) -> Unit = {},
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
        imageCaptureMode = captureStrategy,
        flashMode = flashMode,
        resolutionPreset = resolutionPreset,
        scaleType = scaleType,
        isTorchEnabled = isTorchEnabled,
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
            onSwitchCameraContent(it)
            LaunchedEffect(latestBitmap) {
                onPreviewStreamChanged()
                if (latestBitmap != null) onZoomRatioChanged(cameraState.info.minZoom)
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
    imageCaptureMode: ImageCaptureStrategy,
    flashMode: FlashMode,
    scaleType: ScaleType,
    isTorchEnabled: Boolean,
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