package com.ujizin.camposer.state

import androidx.camera.core.ImageAnalysis
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ujizin.camposer.CameraPreviewImpl

/**
 * Camera State from [CameraPreviewImpl] Composable.
 * */
@Composable
public actual fun rememberCameraState(): CameraState {
    val context = LocalContext.current
    val cameraState = remember { CameraState(context) }
    DisposableEffect(Unit) { onDispose(cameraState::dispose) }
    return cameraState
}

/**
 * Create instance remember of ImageAnalyzer.
 *
 * @see ImageAnalyzer
 * */
@Composable
public fun CameraState.rememberImageAnalyzer(
    imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy = ImageAnalysisBackpressureStrategy.KeepOnlyLatest,
    imageAnalysisTargetSize: ImageTargetSize? = ImageTargetSize(this.imageAnalysisTargetSize),
    imageAnalysisImageQueueDepth: Int = this.imageAnalysisImageQueueDepth,
    analyze: ImageAnalysis.Analyzer,
): ImageAnalyzer = remember(this) {
    ImageAnalyzer(
        this,
        imageAnalysisBackpressureStrategy,
        imageAnalysisTargetSize,
        imageAnalysisImageQueueDepth,
        analyze
    )
}
