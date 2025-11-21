package com.ujizin.camposer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import com.ujizin.camposer.focus.FocusTap
import com.ujizin.camposer.focus.SquareCornerFocus
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.ImplementationMode
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.selector.CamSelector
import kotlinx.coroutines.delay

/**
 * Creates a Camera Preview's composable.
 *
 * @param cameraSession camera state hold some states and camera's controller
 * @param camSelector camera selector to be added, default is back
 * @param captureMode camera capture mode, default is image
 * @param captureStrategy camera image capture mode, default is minimum latency for better performance
 * @param scaleType scale type to be added, default is fill center
 * @param imageAnalyzer image analyzer from camera, see [com.ujizin.camposer.state.properties.ImageAnalyzer]
 * @param implementationMode implementation mode to be added, default is performance
 * @param isImageAnalysisEnabled enable or disable image analysis
 * @param isFocusOnTapEnabled turn on feature focus on tap if true
 * @param isPinchToZoomEnabled turn on feature pinch to zoom if true
 * @param onPreviewStreamChanged dispatch when preview is switching to front or back
 * @param switchCameraContent composable preview when change camera and it's not been streaming yet
 * @param focusTapContent content of focus tap, default is [SquareCornerFocus]
 * @param onFocus callback to use when on focus tap is triggered, call onComplete to [focusTapContent] gone.
 * @param content content composable within of camera preview.
 * @see com.ujizin.camposer.state.properties.ImageAnalyzer
 * @see CameraSession
 * */
@Composable
public fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraSession: CameraSession,
    camSelector: CamSelector = cameraSession.state.camSelector,
    captureMode: CaptureMode = cameraSession.state.captureMode,
    captureStrategy: ImageCaptureStrategy = cameraSession.state.imageCaptureStrategy,
    scaleType: ScaleType = cameraSession.state.scaleType,
    imageAnalyzer: ImageAnalyzer? = null,
    camFormat: CamFormat = cameraSession.state.camFormat,
    implementationMode: ImplementationMode = cameraSession.state.implementationMode,
    isImageAnalysisEnabled: Boolean = imageAnalyzer != null,
    isFocusOnTapEnabled: Boolean = cameraSession.state.isFocusOnTapEnabled,
    isPinchToZoomEnabled: Boolean = cameraSession.state.isPinchToZoomEnabled,
    onPreviewStreamChanged: () -> Unit = {},
    switchCameraContent: @Composable (ImageBitmap) -> Unit = {},
    onFocus: suspend (onComplete: () -> Unit) -> Unit = { onComplete ->
        delay(1000L)
        onComplete()
    },
    focusTapContent: @Composable () -> Unit = { SquareCornerFocus() },
    content: @Composable BoxScope.() -> Unit = {},
) {
    val isCameraIdle by rememberUpdatedState(!cameraSession.isStreaming)
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    var latestBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    Box(modifier = modifier) {
        CameraPreviewImpl(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
            cameraSession = cameraSession,
            captureMode = captureMode,
            camSelector = camSelector,
            imageCaptureStrategy = captureStrategy,
            camFormat = camFormat,
            scaleType = scaleType,
            imageAnalyzer = imageAnalyzer,
            isImageAnalysisEnabled = isImageAnalysisEnabled,
            implementationMode = implementationMode,
            isFocusOnTapEnabled = isFocusOnTapEnabled,
            isPinchToZoomEnabled = isPinchToZoomEnabled,
            onTapFocus = { tapOffset = it },
            onSwitchCamera = { latestBitmap = it },
        )

        FocusTap(
            offset = tapOffset,
            onFocus = { onFocus { tapOffset = Offset.Zero } },
        ) { focusTapContent() }

        CameraSwitchContent(
            modifier = Modifier.fillMaxSize(),
            isCameraIdle = isCameraIdle,
            bitmap = latestBitmap,
            onPreviewStreamChanged = onPreviewStreamChanged,
            onResetBitmap = { latestBitmap = null },
            switchCameraContent = switchCameraContent,
        )

        content()
    }
}

@Composable
private fun CameraSwitchContent(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap?,
    isCameraIdle: Boolean,
    onPreviewStreamChanged: () -> Unit,
    onResetBitmap: () -> Unit,
    switchCameraContent: @Composable (ImageBitmap) -> Unit,
) {
    LaunchedEffect(isCameraIdle) {
        if (!isCameraIdle) onResetBitmap()
    }

    if (!isCameraIdle || bitmap == null) {
        return
    }

    LaunchedEffect(bitmap) { onPreviewStreamChanged() }

    switchCameraContent(bitmap)
}

@Composable
internal expect fun CameraPreviewImpl(
    modifier: Modifier = Modifier,
    cameraSession: CameraSession,
    camSelector: CamSelector,
    captureMode: CaptureMode,
    camFormat: CamFormat,
    imageCaptureStrategy: ImageCaptureStrategy,
    scaleType: ScaleType,
    imageAnalyzer: ImageAnalyzer?,
    implementationMode: ImplementationMode,
    isImageAnalysisEnabled: Boolean,
    isFocusOnTapEnabled: Boolean,
    isPinchToZoomEnabled: Boolean,
    onTapFocus: (Offset) -> Unit,
    onSwitchCamera: (ImageBitmap) -> Unit,
    content: @Composable () -> Unit = {},
)