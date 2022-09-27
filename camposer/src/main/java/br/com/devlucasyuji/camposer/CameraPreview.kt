package br.com.devlucasyuji.camposer

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import br.com.devlucasyuji.camposer.androidview.setOnTapClickListener
import br.com.devlucasyuji.camposer.extensions.observeLatest
import br.com.devlucasyuji.camposer.focus.FocusTap
import br.com.devlucasyuji.camposer.focus.SquareCornerFocus
import br.com.devlucasyuji.camposer.state.CamSelector
import br.com.devlucasyuji.camposer.state.CameraState
import br.com.devlucasyuji.camposer.state.FlashMode
import br.com.devlucasyuji.camposer.state.ImplementationMode
import br.com.devlucasyuji.camposer.state.ScaleType
import br.com.devlucasyuji.camposer.state.rememberCameraState
import kotlinx.coroutines.delay
import androidx.camera.core.CameraSelector as CameraXSelector

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
    camSelector: CamSelector = cameraState.camSelector,
    flashMode: FlashMode = cameraState.flashMode,
    scaleType: ScaleType = cameraState.scaleType,
    enableTorch: Boolean = cameraState.enableTorch,
    implementationMode: ImplementationMode = cameraState.implementationMode,
    isFocusOnTapEnabled: Boolean = cameraState.isFocusOnTapEnabled,
    isPinchToZoomEnabled: Boolean = cameraState.isPinchToZoomEnabled,
    zoomRatio: Float = cameraState.currentZoom,
    onPreviewStreamChanged: () -> Unit = {},
    onSwipeToFront: @Composable (Bitmap) -> Unit = { bitmap ->
        BlurImage(
            modifier = Modifier.fillMaxSize(),
            bitmap = bitmap,
            radius = 20.dp,
            contentDescription = null
        )
    },
    onSwipeToBack: @Composable (Bitmap) -> Unit = { bitmap ->
        BlurImage(
            modifier = Modifier.fillMaxSize(),
            bitmap = bitmap,
            radius = 20.dp,
            contentDescription = null
        )
    },
    onZoomRatioChanged: ((Float) -> Unit)? = null,
    focusTapContent: @Composable () -> Unit = { SquareCornerFocus() },
    content: @Composable () -> Unit = {},
) {
    val cameraIsInitialized by rememberUpdatedState(cameraState.isInitialized)
    val zoomHasChanged by remember(zoomRatio) {
        derivedStateOf { cameraIsInitialized && cameraState.currentZoom != zoomRatio }
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
        camSelector = camSelector,
        flashMode = flashMode,
        scaleType = scaleType,
        enableTorch = enableTorch,
        implementationMode = implementationMode,
        isFocusOnTapEnabled = isFocusOnTapEnabled,
        isPinchToZoomEnabled = isPinchToZoomEnabled,
        onZoomRatioChanged = onZoomRatioChanged,
        focusTapContent = focusTapContent,
        onPreviewStreamChanged = onPreviewStreamChanged,
        onSwipeToFront = onSwipeToFront,
        onSwipeToBack = onSwipeToBack,
        content = content
    )
}

@SuppressLint("RestrictedApi")
@Composable
internal fun CameraPreviewImpl(
    modifier: Modifier,
    cameraState: CameraState,
    cameraIsInitialized: Boolean,
    camSelector: CamSelector,
    flashMode: FlashMode,
    scaleType: ScaleType,
    enableTorch: Boolean,
    implementationMode: ImplementationMode,
    isFocusOnTapEnabled: Boolean,
    isPinchToZoomEnabled: Boolean,
    onZoomRatioChanged: ((Float) -> Unit)?,
    onPreviewStreamChanged: () -> Unit,
    onSwipeToFront: @Composable (Bitmap) -> Unit,
    onSwipeToBack: @Composable (Bitmap) -> Unit,
    focusTapContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    var switchCamera by remember { mutableStateOf(false) }
    var latestBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cameraSelectorState by rememberUpdatedState(camSelector)

    LaunchedEffect(cameraIsInitialized) {
        if (cameraIsInitialized) {
            cameraState.controller.zoomState.observeLatest(lifecycleOwner) { zoom ->
                cameraState.dispatchZoom(zoom, onZoomRatioChanged ?: {})
            }
        }
    }

    AndroidView(modifier = modifier, factory = { context ->
        PreviewView(context).apply {
            this.scaleType = scaleType.type
            this.implementationMode = implementationMode.value
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            controller = cameraState.controller.apply {
                bindToLifecycle(lifecycleOwner)
            }

            previewStreamState.observe(lifecycleOwner) { state ->
                switchCamera = state == PreviewView.StreamState.IDLE
            }
        }
    }, update = { previewView ->
        with(previewView) {
            this.scaleType = scaleType.type
            this.implementationMode = implementationMode.value
            setOnTapClickListener { if (isFocusOnTapEnabled) tapOffset = it }
            if (camSelector != cameraState.camSelector) latestBitmap = bitmap
        }

        if (cameraIsInitialized) {
            with(cameraState) {
                this.camSelector = camSelector
                this.scaleType = scaleType
                this.implementationMode = implementationMode
                this.isFocusOnTapEnabled = isFocusOnTapEnabled
                this.isPinchToZoomEnabled = isPinchToZoomEnabled
                this.flashMode = flashMode
                this.enableTorch = enableTorch
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

    if (switchCamera) {
        latestBitmap?.let {
            when (cameraSelectorState.selector.lensFacing) {
                CameraXSelector.LENS_FACING_FRONT -> onSwipeToFront(it)
                CameraXSelector.LENS_FACING_BACK -> onSwipeToBack(it)
                else -> Unit
            }
            LaunchedEffect(latestBitmap) {
                onPreviewStreamChanged()
            }
        }
    }

    content()
}
