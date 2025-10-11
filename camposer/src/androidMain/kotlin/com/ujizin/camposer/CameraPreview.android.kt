package com.ujizin.camposer

import android.view.ViewGroup
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ujizin.camposer.config.properties.CamSelector
import com.ujizin.camposer.config.properties.CaptureMode
import com.ujizin.camposer.config.properties.FlashMode
import com.ujizin.camposer.config.properties.ImageAnalyzer
import com.ujizin.camposer.config.properties.ImageCaptureStrategy
import com.ujizin.camposer.config.properties.ImplementationMode
import com.ujizin.camposer.config.properties.ResolutionPreset
import com.ujizin.camposer.config.properties.ScaleType
import com.ujizin.camposer.config.update
import com.ujizin.camposer.controller.zoom.PinchToZoomController
import com.ujizin.camposer.extensions.setCameraTouchEvent
import com.ujizin.camposer.focus.SquareCornerFocus
import com.ujizin.camposer.state.CameraState

/**
 * Creates a Camera Preview's composable.
 *
 * @param cameraState camera state hold some states and camera's controller, it can be useful to given action like [CameraState.takePicture]
 * @param camSelector camera selector to be added, default is back
 * @param captureMode camera capture mode, default is image
 * @param imageCaptureMode camera image capture mode, default is minimum latency for better performance
 * @param flashMode flash mode to be added, default is off
 * @param scaleType scale type to be added, default is fill center
 * @param isTorchEnabled enable torch from camera, default is false.
 * @param exposureCompensation camera exposure compensation to be added
 * @param zoomRatio zoom ratio to be added, default is 1.0
 * @param imageAnalyzer image analyzer from camera, see [ImageAnalyzer]
 * @param implementationMode implementation mode to be added, default is performance
 * @param isImageAnalysisEnabled enable or disable image analysis
 * @param isFocusOnTapEnabled turn on feature focus on tap if true
 * @param isPinchToZoomEnabled turn on feature pinch to zoom if true
 * @param onZoomRatioChanged dispatch when zoom is changed by pinch to zoom
 * @param focusTapContent content of focus tap, default is [SquareCornerFocus]
 * @param content content composable within of camera preview.
 * @see ImageAnalyzer
 * @see CameraState
 * */
@Composable
internal actual fun CameraPreviewImpl(
    modifier: Modifier,
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
    onZoomRatioChanged: (Float) -> Unit,
    focusTapContent: @Composable (() -> Unit),
    content: @Composable (() -> Unit),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleEvent by lifecycleOwner.lifecycle.observeAsState()
    val cameraIsInitialized by rememberUpdatedState(cameraState.isInitialized)
    val isCameraIdle by rememberUpdatedState(!cameraState.isStreaming)
    var latestBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var cameraOffset by remember { mutableStateOf(Offset.Zero) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(latestBitmap) { latestBitmap?.let(onSwitchCamera) }

    LaunchedEffect(cameraState, previewViewRef) {
        val previewView = previewViewRef ?: return@LaunchedEffect
        previewView.onViewBind(
            cameraState = cameraState,
            lifecycleOwner = lifecycleOwner,
            zoomRatio = zoomRatio,
            onZoomRatioChanged = onZoomRatioChanged,
            onTapFocus = {
                if (cameraState.config.isFocusOnTapEnabled) {
                    onTapFocus(it + cameraOffset)
                }
            },
        )
    }

    AndroidView(
        modifier = modifier.onGloballyPositioned { cameraOffset = it.positionInParent() },
        factory = { context ->
            PreviewView(context).apply { previewViewRef = this }
        },
        update = { previewView ->
            if (!cameraIsInitialized) return@AndroidView
            with(previewView) {
                if (this.scaleType != scaleType.type) {
                    this.scaleType = scaleType.type
                }
                if (this.implementationMode != implementationMode.value) {
                    this.implementationMode = implementationMode.value
                }
                latestBitmap = when {
                    lifecycleEvent == Lifecycle.Event.ON_STOP -> null
                    !isCameraIdle && camSelector != cameraState.config.camSelector -> bitmap?.asImageBitmap()
                    else -> latestBitmap
                }

                cameraState.config.update(
                    camSelector = camSelector,
                    captureMode = captureMode,
                    scaleType = scaleType,
                    isImageAnalysisEnabled = isImageAnalysisEnabled,
                    imageAnalyzer = imageAnalyzer,
                    implementationMode = implementationMode,
                    isFocusOnTapEnabled = isFocusOnTapEnabled,
                    flashMode = flashMode,
                    isTorchEnabled = isTorchEnabled,
                    zoomRatio = zoomRatio,
                    imageCaptureStrategy = imageCaptureMode,
                    resolutionPreset = resolutionPreset,
                    exposureCompensation = exposureCompensation,
                    isPinchToZoomEnabled = isPinchToZoomEnabled,
                )
            }
        },
    )

    content()
}

private fun PreviewView.onViewBind(
    cameraState: CameraState,
    lifecycleOwner: LifecycleOwner,
    zoomRatio: Float,
    onZoomRatioChanged: (Float) -> Unit,
    onTapFocus: (Offset) -> Unit,
) {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
    )
    controller = cameraState.controller.apply {
        bindToLifecycle(lifecycleOwner)
    }

    previewStreamState.observe(lifecycleOwner) { state ->
        cameraState.isStreaming = state == PreviewView.StreamState.STREAMING
    }

    setCameraTouchEvent(
        pinchZoomController = PinchToZoomController(
            cameraState = cameraState,
            zoomRatio = zoomRatio,
            onZoomRatioChanged = onZoomRatioChanged
        ),
        onTap = onTapFocus,
    )

}
