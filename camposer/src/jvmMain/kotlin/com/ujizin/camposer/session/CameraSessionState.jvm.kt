package com.ujizin.camposer.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.state.properties.ImageAnalyzer

/**
 * Camera State from [com.ujizin.camposer.CameraPreview] Composable.
 * */
@Composable
public actual fun rememberCameraSession(controller: CameraController): CameraSession {
  val session = remember(controller) { CameraSession(controller) }
  DisposableEffect(session) { onDispose(session::dispose) }
  return session
}

/**
 * Create instance remember of ImageAnalyzer.
 *
 * @param analyzer receives each camera frame on a background thread.
 * @see com.ujizin.camposer.state.properties.ImageAnalyzer
 * */
@Composable
public fun CameraSession.rememberImageAnalyzer(analyzer: ImageAnalyzer.Analyzer): ImageAnalyzer =
  remember(this, analyzer) { ImageAnalyzer(analyzer) }
