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
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ujizin.camposer.extensions.clamped
import com.ujizin.camposer.extensions.onCameraTouchEvent
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
 * @param videoQualitySelector quality selector to the video capture
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
internal actual fun CameraPreviewImpl(
    modifier: Modifier,
    cameraState: CameraState,
    camSelector: CamSelector,
    captureMode: CaptureMode,
    resolutionPreset: ResolutionPreset,
    imageCaptureMode: ImageCaptureMode,
    imageCaptureTargetSize: ImageTargetSize?,
    flashMode: FlashMode,
    scaleType: ScaleType,
    enableTorch: Boolean,
    exposureCompensation: Int,
    zoomRatio: Float,
    imageAnalyzer: ImageAnalyzer?,
    implementationMode: ImplementationMode,
    isImageAnalysisEnabled: Boolean,
    isFocusOnTapEnabled: Boolean,
    isPinchToZoomEnabled: Boolean,
    onPreviewStreamChanged: () -> Unit,
    onTapFocus: (Offset) -> Unit,
    onSwitchCamera: (ImageBitmap) -> Unit,
    onSwitchToFront: @Composable ((ImageBitmap) -> Unit),
    onSwitchToBack: @Composable ((ImageBitmap) -> Unit),
    onFocus: suspend (() -> Unit) -> Unit,
    onZoomRatioChanged: (Float) -> Unit,
    focusTapContent: @Composable (() -> Unit),
    content: @Composable (() -> Unit)
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleEvent by lifecycleOwner.lifecycle.observeAsState()
    val cameraIsInitialized by rememberUpdatedState(cameraState.isInitialized)
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    val isCameraIdle by rememberUpdatedState(!cameraState.isStreaming)
    var latestBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var cameraOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(tapOffset) { onTapFocus(tapOffset) }
    LaunchedEffect(latestBitmap) { latestBitmap?.let(onSwitchCamera) }

    AndroidView(
        modifier = modifier.onGloballyPositioned { cameraOffset = it.positionInParent() },
        factory = { context ->
            PreviewView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
                controller = cameraState.controller.apply {
                    bindToLifecycle(lifecycleOwner)
                }

                previewStreamState.observe(lifecycleOwner) { state ->
                    cameraState.isStreaming = state == PreviewView.StreamState.STREAMING
                }
            }
        },
        update = { previewView ->
            if (cameraIsInitialized) {
                with(previewView) {
                    if (this.scaleType != scaleType.type) {
                        this.scaleType = scaleType.type
                    }
                    this.implementationMode = implementationMode.value
                    onCameraTouchEvent(
                        onTap = { if (isFocusOnTapEnabled) tapOffset = it + cameraOffset },
                        onScaleChanged = {
                            if (isPinchToZoomEnabled) {
                                val zoom = zoomRatio.clamped(it).coerceIn(
                                    minimumValue = cameraState.minZoom,
                                    maximumValue = cameraState.maxZoom
                                )
                                onZoomRatioChanged(zoom)
                            }
                        }
                    )
                    latestBitmap = when {
                        lifecycleEvent == Lifecycle.Event.ON_STOP -> null
                        !isCameraIdle && camSelector != cameraState.camSelector -> bitmap?.asImageBitmap()
                        else -> latestBitmap
                    }
                    cameraState.update(
                        camSelector = camSelector,
                        captureMode = captureMode,
                        imageCaptureTargetSize = imageCaptureTargetSize,
                        scaleType = scaleType,
                        isImageAnalysisEnabled = isImageAnalysisEnabled,
                        imageAnalyzer = imageAnalyzer,
                        implementationMode = implementationMode,
                        isFocusOnTapEnabled = isFocusOnTapEnabled,
                        flashMode = flashMode,
                        enableTorch = enableTorch,
                        zoomRatio = zoomRatio,
                        imageCaptureMode = imageCaptureMode,
                        meteringPoint = meteringPointFactory.createPoint(x, y),
                        resolutionPreset = resolutionPreset,
                        exposureCompensation = exposureCompensation,
                    )
                }

            }
        },
    )

    content()
}
