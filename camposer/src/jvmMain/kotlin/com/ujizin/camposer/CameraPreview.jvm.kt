package com.ujizin.camposer

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.ujizin.camposer.internal.preview.PreviewGeometry
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.ImplementationMode
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.isMirrorEnabled
import com.ujizin.camposer.state.properties.selector.CamSelector
import com.ujizin.camposer.state.update
import kotlin.math.roundToInt

/**
 * Desktop camera preview. Draws the latest frame on a [Canvas].
 *
 * Focus on tap and pinch to zoom are not supported by desktop webcams, so the gestures are
 * not installed.
 */
@Composable
internal actual fun CameraPreviewImpl(
  modifier: Modifier,
  cameraSession: CameraSession,
  camSelector: CamSelector,
  captureMode: CaptureMode,
  camFormat: CamFormat,
  imageCaptureStrategy: ImageCaptureStrategy,
  scaleType: ScaleType,
  imageAnalyzer: ImageAnalyzer?,
  implementationMode: ImplementationMode,
  isImageAnalysisEnabled: Boolean,
  isFocusOnTapEnabled: Boolean,
  isPinchToZoomEnabled: Boolean,
  previewBackgroundColor: Color,
  onTapFocus: (Offset) -> Unit,
  onSwitchCamera: (ImageBitmap) -> Unit,
  content: @Composable () -> Unit,
) {
  val density = LocalDensity.current.density
  val frameState = cameraSession.currentFrame.collectAsState()
  val mirrorMode by cameraSession.state.mirrorMode.collectAsState()
  val currentCamSelector by cameraSession.state.camSelector.collectAsState()
  val isCameraIdle by rememberUpdatedState(!cameraSession.isStreaming)
  var latestBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
  val isMirrored = mirrorMode.isMirrorEnabled(currentCamSelector.camPosition)

  LaunchedEffect(latestBitmap) { latestBitmap?.let(onSwitchCamera) }

  DisposableEffect(cameraSession) {
    cameraSession.startCamera()
    onDispose {
      cameraSession.stopCamera()
      cameraSession.updatePreviewGeometry(null)
    }
  }

  SideEffect {
    if (!isCameraIdle && camSelector != currentCamSelector) {
      latestBitmap = frameState.value
    }

    cameraSession.update(
      camSelector = camSelector,
      captureMode = captureMode,
      scaleType = scaleType,
      isImageAnalysisEnabled = isImageAnalysisEnabled,
      imageAnalyzer = imageAnalyzer,
      implementationMode = implementationMode,
      isFocusOnTapEnabled = isFocusOnTapEnabled,
      imageCaptureStrategy = imageCaptureStrategy,
      camFormat = camFormat,
      isPinchToZoomEnabled = isPinchToZoomEnabled,
    )
  }

  var publishedGeometry by remember { mutableStateOf<PreviewGeometry?>(null) }

  Canvas(modifier = modifier) {
    if (previewBackgroundColor != Color.Unspecified) {
      drawRect(color = previewBackgroundColor)
    }

    val bitmap = frameState.value ?: return@Canvas
    val geometry = PreviewGeometry.compute(
      imageWidth = bitmap.width,
      imageHeight = bitmap.height,
      canvasWidth = size.width,
      canvasHeight = size.height,
      scaleType = scaleType,
      isMirrored = isMirrored,
      density = density,
    ) ?: return@Canvas

    if (geometry != publishedGeometry) {
      publishedGeometry = geometry
      cameraSession.updatePreviewGeometry(geometry)
    }

    drawFrame(bitmap, geometry)
  }

  content()
}

private fun DrawScope.drawFrame(
  bitmap: ImageBitmap,
  geometry: PreviewGeometry,
) {
  val dstOffset = IntOffset(geometry.offsetX.roundToInt(), geometry.offsetY.roundToInt())
  val dstSize = IntSize(geometry.drawWidth.roundToInt(), geometry.drawHeight.roundToInt())

  if (!geometry.isMirrored) {
    drawImage(image = bitmap, dstOffset = dstOffset, dstSize = dstSize)
    return
  }

  val pivot = Offset(
    x = geometry.offsetX + geometry.drawWidth / 2F,
    y = geometry.offsetY + geometry.drawHeight / 2F,
  )
  withTransform({ scale(scaleX = -1F, scaleY = 1F, pivot = pivot) }) {
    drawImage(image = bitmap, dstOffset = dstOffset, dstSize = dstSize)
  }
}
