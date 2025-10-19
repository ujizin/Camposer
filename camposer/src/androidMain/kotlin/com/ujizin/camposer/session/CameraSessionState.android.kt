package com.ujizin.camposer.session

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ujizin.camposer.CameraPreviewImpl
import com.ujizin.camposer.config.properties.ImageAnalysisBackpressureStrategy
import com.ujizin.camposer.config.properties.ImageAnalyzer
import com.ujizin.camposer.controller.camera.CameraController

/**
 * Camera State from [CameraPreviewImpl] Composable.
 * */
@Composable
public actual fun rememberCameraSession(controller: CameraController): CameraSession {
    val context = LocalContext.current
    val cameraSession = remember(controller) { CameraSession(context, controller) }
    DisposableEffect(Unit) { onDispose(cameraSession::dispose) }
    return cameraSession
}

/**
 * Create instance remember of ImageAnalyzer.
 *
 * @see com.ujizin.camposer.config.properties.ImageAnalyzer
 * */
@Composable
public fun CameraSession.rememberImageAnalyzer(
    imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy = ImageAnalysisBackpressureStrategy.KeepOnlyLatest,
    imageAnalysisResolutionSelector: ResolutionSelector? = controller.imageAnalysisResolutionSelector,
    imageAnalysisImageQueueDepth: Int = controller.imageAnalysisImageQueueDepth,
    analyze: ImageAnalysis.Analyzer,
): ImageAnalyzer = remember(this) {
    ImageAnalyzer(
        controller,
        imageAnalysisBackpressureStrategy,
        imageAnalysisResolutionSelector,
        imageAnalysisImageQueueDepth,
        analyze
    )
}
