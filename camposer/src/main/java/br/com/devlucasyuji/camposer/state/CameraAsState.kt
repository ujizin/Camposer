package br.com.devlucasyuji.camposer.state

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner

/**
 * Camera State from [CameraPreview] Composable.
 * */
@Composable
fun rememberCameraState(): CameraState {
    val lifecycleOwner = LocalLifecycleOwner.current
    return remember { CameraState(lifecycleOwner) }
}

/**
 * Camera selector's State to [CameraPreview] Composable.
 * */
@Composable
fun rememberCameraSelector(
    selector: CamSelector = CamSelector.Back
): MutableState<CamSelector> = remember {
    mutableStateOf(selector)
}

/**
 * Custom camera selector's State to [CameraPreview] Composable.
 * */
@Composable
fun rememberCameraSelector(
    block: () -> CameraSelector
): MutableState<CamSelector> = remember {
    mutableStateOf(customCamSelector(block))
}

fun customCamSelector(
    block: () -> CameraSelector
): CamSelector = CamSelector.CustomSelector(block())

/**
 * Flash mode's State to [CameraPreview] Composable.
 * */
@Composable
fun CameraState.rememberFlashMode(
    initialFlashMode: FlashMode,
    useSaver: Boolean = true
): MutableState<FlashMode> = rememberConditionalState(
    initialValue = initialFlashMode,
    defaultValue = FlashMode.Off,
    useSaver = useSaver,
    predicate = hasFlashUnit
)

/**
 * Torch's State to [CameraPreview] Composable.
 * */
@Composable
fun CameraState.rememberTorch(
    initialTorch: Boolean,
    useSaver: Boolean = true
): MutableState<Boolean> = rememberConditionalState(
    initialValue = initialTorch,
    defaultValue = false,
    useSaver = useSaver,
    predicate = hasFlashUnit
)

/**
 * Create instance remember of ImageAnalyzer.
 *
 * @see ImageAnalyzer
 * */
@Composable
fun CameraState.rememberImageAnalyzer(
    imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy = ImageAnalysisBackpressureStrategy.KeepOnlyLatest,
    imageAnalysisTargetSize: Size? = this.imageAnalysisTargetSize?.resolution,
    imageAnalysisImageQueueDepth: Int = this.imageAnalysisImageQueueDepth,
    analyze: (ImageProxy) -> Unit,
): ImageAnalyzer = remember(this) {
    ImageAnalyzer(
        this,
        imageAnalysisBackpressureStrategy,
        imageAnalysisTargetSize,
        imageAnalysisImageQueueDepth,
        analyze
    )
}
