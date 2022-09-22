package br.com.devlucasyuji.camposer

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import br.com.devlucasyuji.camposer.androidview.setOnTapClickListener
import br.com.devlucasyuji.camposer.extensions.observeLatest
import br.com.devlucasyuji.camposer.focus.FocusTap
import br.com.devlucasyuji.camposer.focus.SquareCornerFocus
import kotlinx.coroutines.delay

/**
 * Creates a Camera Preview's composable.
 *
 * @param cameraState camera state hold some states and camera's controller, it can be useful to given action like [CameraState.takePicture]
 * @see CameraState
 * */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraState: CameraState = rememberCameraState(),
    cameraSelector: CameraSelector = cameraState.cameraSelector,
    flashMode: FlashMode = cameraState.flashMode,
    scaleType: ScaleType = cameraState.scaleType,
    enableTorch: Boolean = cameraState.enableTorch,
    isFocusOnTapEnabled: Boolean = cameraState.isFocusOnTapEnabled,
    isPinchToZoomEnabled: Boolean = cameraState.isPinchToZoomEnabled,
    zoomRatio: Float = cameraState.currentZoomRatio,
    onZoomRatioChanged: ((Float) -> Unit)? = null,
    focusTapContent: @Composable () -> Unit = { SquareCornerFocus() },
    content: @Composable () -> Unit = {},
) {
    val cameraIsInitialized by rememberUpdatedState(cameraState.isInitialized)
    val zoomHasChanged by remember(zoomRatio) {
        derivedStateOf { cameraIsInitialized && cameraState.currentZoomRatio != zoomRatio }
    }

    LaunchedEffect(zoomHasChanged) {
        if (zoomHasChanged) {
            cameraState.setZoomRatio(zoomRatio)
        }
    }

    CameraPreviewImpl(
        modifier = modifier,
        cameraState = cameraState,
        cameraIsInitialized = cameraIsInitialized,
        cameraSelector = cameraSelector,
        flashMode = flashMode,
        scaleType = scaleType,
        enableTorch = enableTorch,
        isFocusOnTapEnabled = isFocusOnTapEnabled,
        isPinchToZoomEnabled = isPinchToZoomEnabled,
        zoomRatio = zoomRatio,
        onZoomRatioChanged = onZoomRatioChanged,
        focusTapContent = focusTapContent,
        content = content
    )
}

@Composable
internal fun CameraPreviewImpl(
    modifier: Modifier,
    cameraState: CameraState,
    cameraIsInitialized: Boolean,
    cameraSelector: CameraSelector,
    flashMode: FlashMode,
    scaleType: ScaleType,
    enableTorch: Boolean,
    isFocusOnTapEnabled: Boolean,
    isPinchToZoomEnabled: Boolean,
    zoomRatio: Float,
    onZoomRatioChanged: ((Float) -> Unit)?,
    focusTapContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var tapOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(cameraIsInitialized) {
        if (cameraIsInitialized) {
            cameraState.controller.zoomState.observeLatest(lifecycleOwner) { zoom ->
                cameraState.dispatchZoom(zoom.zoomRatio) {
                    onZoomRatioChanged?.invoke(it)
                }
            }
        }
    }

    AndroidView(modifier = modifier, factory = { context ->
        PreviewView(context).apply {
            this.scaleType = scaleType.type
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            controller = cameraState.controller.apply {
                bindToLifecycle(lifecycleOwner)
            }
        }
    }, update = { previewView ->
        previewView.scaleType = scaleType.type
        previewView.setOnTapClickListener {
            if (isFocusOnTapEnabled) tapOffset = it
        }
        if (cameraIsInitialized) {
            with(cameraState) {
                this.cameraSelector = cameraSelector
                this.flashMode = flashMode
                this.scaleType = scaleType
                this.enableTorch = enableTorch
                this.currentZoomRatio = zoomRatio
                this.isFocusOnTapEnabled = isFocusOnTapEnabled
                this.isPinchToZoomEnabled = isPinchToZoomEnabled
            }
        }
    })

    FocusTap(
        offset = tapOffset,
        onAfterFocus = {
            delay(1000L)
            tapOffset = Offset.Zero
        },
    ) { focusTapContent() }

    content()
}
