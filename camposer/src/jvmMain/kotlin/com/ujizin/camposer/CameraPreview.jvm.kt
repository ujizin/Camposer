package com.ujizin.camposer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.ImplementationMode
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.selector.CamSelector
import com.ujizin.camposer.state.update

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

  val frame by cameraSession.currentFrame.collectAsState()

  Box(modifier = modifier) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val bitmap = frame
      if (bitmap != null) {
        drawCameraFrame(bitmap, scaleType)
      } else if (previewBackgroundColor != Color.Unspecified) {
        drawRect(color = previewBackgroundColor)
      }
    }
    content()
  }
}

private fun DrawScope.drawCameraFrame(
  bitmap: ImageBitmap,
  scaleType: ScaleType,
) {
  val canvasWidth = size.width
  val canvasHeight = size.height
  val bitmapWidth = bitmap.width.toFloat()
  val bitmapHeight = bitmap.height.toFloat()

  if (bitmapWidth <= 0f || bitmapHeight <= 0f) return

  val scale = when (scaleType) {
    ScaleType.FillStart,
    ScaleType.FillCenter,
    ScaleType.FillEnd,
    -> maxOf(canvasWidth / bitmapWidth, canvasHeight / bitmapHeight)

    ScaleType.FitStart,
    ScaleType.FitCenter,
    ScaleType.FitEnd,
    -> minOf(canvasWidth / bitmapWidth, canvasHeight / bitmapHeight)
  }

  val scaledWidth = bitmapWidth * scale
  val scaledHeight = bitmapHeight * scale

  val offsetX = when (scaleType) {
    ScaleType.FitStart,
    ScaleType.FillStart,
    -> 0f

    ScaleType.FitCenter,
    ScaleType.FillCenter,
    -> (canvasWidth - scaledWidth) / 2f

    ScaleType.FitEnd,
    ScaleType.FillEnd,
    -> canvasWidth - scaledWidth
  }

  val offsetY = when (scaleType) {
    ScaleType.FitStart,
    ScaleType.FillStart,
    -> 0f

    ScaleType.FitCenter,
    ScaleType.FillCenter,
    -> (canvasHeight - scaledHeight) / 2f

    ScaleType.FitEnd,
    ScaleType.FillEnd,
    -> canvasHeight - scaledHeight
  }

  drawImage(
    image = bitmap,
    dstOffset = IntOffset(offsetX.toInt(), offsetY.toInt()),
    dstSize = IntSize(scaledWidth.toInt(), scaledHeight.toInt()),
  )
}
