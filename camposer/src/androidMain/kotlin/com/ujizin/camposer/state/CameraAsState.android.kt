package com.ujizin.camposer.state

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ujizin.camposer.CameraPreviewImpl
import com.ujizin.camposer.controller.camera.CameraController

/**
 * Camera State from [CameraPreviewImpl] Composable.
 * */
@Composable
public actual fun rememberCameraState(controller: CameraController): CameraState {
    val context = LocalContext.current
    val cameraState = remember(controller) { CameraState(context, controller) }
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
    imageAnalysisResolutionSelector: ResolutionSelector? = this.imageAnalysisResolutionSelector,
    imageAnalysisImageQueueDepth: Int = this.imageAnalysisImageQueueDepth,
    analyze: ImageAnalysis.Analyzer,
): ImageAnalyzer = remember(this) {
    ImageAnalyzer(
        this,
        imageAnalysisBackpressureStrategy,
        imageAnalysisResolutionSelector,
        imageAnalysisImageQueueDepth,
        analyze
    )
}
