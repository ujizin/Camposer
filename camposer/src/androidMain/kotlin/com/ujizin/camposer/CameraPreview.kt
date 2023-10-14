package com.ujizin.camposer

import android.annotation.SuppressLint
import android.graphics.Bitmap
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import com.ujizin.camposer.extensions.clamped
import com.ujizin.camposer.extensions.onCameraTouchEvent
import com.ujizin.camposer.focus.FocusTap
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.CaptureMode
import com.ujizin.camposer.state.FlashMode
import com.ujizin.camposer.state.ImageAnalyzer
import com.ujizin.camposer.state.ImageCaptureMode
import com.ujizin.camposer.state.ImageTargetSize
import com.ujizin.camposer.state.ImplementationMode
import com.ujizin.camposer.state.ScaleType
import androidx.camera.core.CameraSelector as CameraXSelector

@Composable
public actual fun CameraPreview(
    modifier: Modifier,
    cameraState: CameraState,
    camSelector: CamSelector,
    captureMode: CaptureMode,
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
    onSwitchToFront: @Composable (ImageBitmap) -> Unit,
    onSwitchToBack: @Composable (ImageBitmap) -> Unit,
    onFocus: suspend (onComplete: () -> Unit) -> Unit,
    onZoomRatioChanged: (Float) -> Unit,
    focusTapContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    CameraPreviewImpl(
        modifier = modifier,
        cameraState = cameraState,
        camSelector = camSelector,
        captureMode = captureMode,
        exposureCompensation = exposureCompensation,
        imageCaptureMode = imageCaptureMode,
        imageCaptureTargetSize = imageCaptureTargetSize,
        flashMode = flashMode,
        scaleType = scaleType,
        enableTorch = enableTorch,
        zoomRatio = zoomRatio,
        imageAnalyzer = imageAnalyzer,
        isImageAnalysisEnabled = isImageAnalysisEnabled,
        implementationMode = implementationMode,
        isFocusOnTapEnabled = isFocusOnTapEnabled,
        isPinchToZoomEnabled = isPinchToZoomEnabled,
        onZoomRatioChanged = onZoomRatioChanged,
        focusTapContent = focusTapContent,
        onFocus = onFocus,
        onPreviewStreamChanged = onPreviewStreamChanged,
        onSwipeToFront = onSwitchToFront,
        onSwipeToBack = onSwitchToBack,
        content = content
    )
}

@SuppressLint("RestrictedApi")
@Composable
internal fun CameraPreviewImpl(
    modifier: Modifier,
    cameraState: CameraState,
    camSelector: CamSelector,
    captureMode: CaptureMode,
    imageCaptureMode: ImageCaptureMode,
    imageCaptureTargetSize: ImageTargetSize?,
    flashMode: FlashMode,
    scaleType: ScaleType,
    enableTorch: Boolean,
    zoomRatio: Float,
    implementationMode: ImplementationMode,
    imageAnalyzer: ImageAnalyzer?,
    exposureCompensation: Int,
    isImageAnalysisEnabled: Boolean,
    isFocusOnTapEnabled: Boolean,
    isPinchToZoomEnabled: Boolean,
    onZoomRatioChanged: (Float) -> Unit,
    onPreviewStreamChanged: () -> Unit,
    onFocus: suspend (() -> Unit) -> Unit,
    onSwipeToFront: @Composable (ImageBitmap) -> Unit,
    onSwipeToBack: @Composable (ImageBitmap) -> Unit,
    focusTapContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleEvent by lifecycleOwner.lifecycle.observeAsState()
    val cameraIsInitialized by rememberUpdatedState(cameraState.isInitialized)
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    val isCameraIdle by rememberUpdatedState(!cameraState.isStreaming)
    var latestBitmap by remember { mutableStateOf<Bitmap?>(null) }

    AndroidView(modifier = modifier, factory = { context ->
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
    }, update = { previewView ->
        if (cameraIsInitialized) {
            with(previewView) {
                this.scaleType = scaleType.type
                this.implementationMode = implementationMode.value
                onCameraTouchEvent(
                    onTap = { if (isFocusOnTapEnabled) tapOffset = it },
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
                    !isCameraIdle && camSelector != cameraState.camSelector -> bitmap
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
                    exposureCompensation = exposureCompensation,
                )
            }

        }
    })

    FocusTap(
        offset = tapOffset,
        onFocus = { onFocus { tapOffset = Offset.Zero } },
    ) { focusTapContent() }

    if (isCameraIdle) {
        latestBitmap?.let {
            val imageBitmap = it.asImageBitmap()
            when (camSelector.selector.lensFacing) {
                CameraXSelector.LENS_FACING_FRONT -> onSwipeToFront(imageBitmap)
                CameraXSelector.LENS_FACING_BACK -> onSwipeToBack(imageBitmap)
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