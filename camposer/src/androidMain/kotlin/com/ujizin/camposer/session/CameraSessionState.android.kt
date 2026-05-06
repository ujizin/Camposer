package com.ujizin.camposer.session

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.state.properties.ImageAnalysisBackpressureStrategy
import com.ujizin.camposer.state.properties.ImageAnalysisBackpressureStrategy.KeepOnlyLatest
import com.ujizin.camposer.state.properties.ImageAnalyzer

/**
 * Camera State from [CameraPreview] Composable.
 * */
@Composable
public actual fun rememberCameraSession(controller: CameraController): CameraSession {
  val context = LocalContext.current
  val cameraSession = remember(controller) { CameraSession(context, controller) }
  DisposableEffect(cameraSession) { onDispose(cameraSession::dispose) }
  return cameraSession
}

/**
 * Create instance remember of ImageAnalyzer.
 *
 * @see com.ujizin.camposer.state.properties.ImageAnalyzer
 * */
@Composable
public fun CameraSession.rememberImageAnalyzer(
  imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy = KeepOnlyLatest,
  imageAnalysisResolutionSelector: ResolutionSelector? = cameraXController
    .imageAnalysisResolutionSelector,
  imageAnalysisImageQueueDepth: Int = cameraXController.imageAnalysisImageQueueDepth,
  analyze: ImageAnalysis.Analyzer,
): ImageAnalyzer =
  remember(this) {
    // Pre-register the analyzer with the wrapper before bindToLifecycle() so that
    // CameraXControllerWrapper can set mAnalysisAnalyzer on the raw CameraController
    // (requires mSessionConfig == null, which holds before the first setSessionConfig() call).
    cameraXControllerWrapper.setImageAnalysisAnalyzer(
      cameraXControllerWrapper.mainExecutor,
      analyze,
    )
    ImageAnalyzer(
      cameraXController,
      imageAnalysisBackpressureStrategy,
      imageAnalysisResolutionSelector,
      imageAnalysisImageQueueDepth,
      analyze,
    )
  }
