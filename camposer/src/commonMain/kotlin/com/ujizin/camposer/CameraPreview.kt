package com.ujizin.camposer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.ImplementationMode
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.format.Default
import com.ujizin.camposer.state.properties.selector.CamSelector
import com.ujizin.camposer.ui.focus.FocusTap
import com.ujizin.camposer.ui.focus.SquareCornerFocus
import kotlinx.coroutines.delay

/**
 * Creates a Camera Preview's composable.
 *
 * @param cameraSession camera session, hold state, info and camera's controller
 * @param camSelector camera selector to be added, default is [CamSelector.Back]
 * @param captureMode camera capture mode, default is [CaptureMode.Image]
 * @param captureStrategy camera image capture mode, default is [ImageCaptureStrategy.MinLatency] for better performance
 * @param scaleType scale type to be added, default is [ScaleType.FillCenter]
 * @param imageAnalyzer image analyzer from camera, see [com.ujizin.camposer.state.properties.ImageAnalyzer]
 * @param camFormat camera format, default is [CamFormat.Companion.Default]
 * @param implementationMode implementation mode to be added, default is [ImplementationMode.Performance] (Android Only)
 * @param isImageAnalysisEnabled enable or disable image analysis
 * @param isFocusOnTapEnabled turn on feature focus on tap if true and supported
 * @param isPinchToZoomEnabled turn on feature pinch to zoom if true and supported
 * @param previewBackgroundColor The preview background color to be added. This is necessary because
 * non-fill scale types can display bars with the system background color, and it is not overridden
 * by the modifier color.
 * @param onPreviewStreamChanged dispatch when preview is switching to front or back (Android only)
 * @param switchCameraContent composable preview when change camera, and it's not been streaming yet (Android only)
 * @param focusTapContent content of focus tap, default is [SquareCornerFocus]
 * @param onFocus callback to use when on focus tap is triggered, call onComplete to [focusTapContent] gone.
 * @param content content composable within of camera preview.
 *
 * @see CameraSession
 * */
@Composable
public fun CameraPreview(
  modifier: Modifier = Modifier,
  cameraSession: CameraSession,
  camSelector: CamSelector = cameraSession.state.camSelector.value,
  captureMode: CaptureMode = cameraSession.state.captureMode.value,
  captureStrategy: ImageCaptureStrategy = cameraSession.state.imageCaptureStrategy.value,
  scaleType: ScaleType = cameraSession.state.scaleType.value,
  imageAnalyzer: ImageAnalyzer? = null,
  camFormat: CamFormat = cameraSession.state.camFormat.value,
  implementationMode: ImplementationMode = cameraSession.state.implementationMode.value,
  isImageAnalysisEnabled: Boolean = imageAnalyzer != null,
  isFocusOnTapEnabled: Boolean = cameraSession.state.isFocusOnTapEnabled.value,
  isPinchToZoomEnabled: Boolean = cameraSession.state.isPinchToZoomEnabled.value,
  previewBackgroundColor: Color = Color.Unspecified,
  onPreviewStreamChanged: () -> Unit = {},
  switchCameraContent: @Composable (ImageBitmap) -> Unit = {},
  onFocus: suspend (onComplete: () -> Unit) -> Unit = { onComplete ->
    delay(1000L)
    onComplete()
  },
  focusTapContent: @Composable () -> Unit = { SquareCornerFocus() },
  content: @Composable BoxScope.() -> Unit = {},
) {
  val isCameraIdle by rememberUpdatedState(!cameraSession.isStreaming)
  var tapOffset by remember { mutableStateOf(Offset.Zero) }
  var latestBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

  Box(modifier = modifier) {
    CameraPreviewImpl(
      modifier = Modifier
        .fillMaxSize()
        .clipToBounds(),
      cameraSession = cameraSession,
      captureMode = captureMode,
      camSelector = camSelector,
      imageCaptureStrategy = captureStrategy,
      camFormat = camFormat,
      scaleType = scaleType,
      imageAnalyzer = imageAnalyzer,
      isImageAnalysisEnabled = isImageAnalysisEnabled,
      implementationMode = implementationMode,
      isFocusOnTapEnabled = isFocusOnTapEnabled,
      isPinchToZoomEnabled = isPinchToZoomEnabled,
      previewBackgroundColor = previewBackgroundColor,
      onTapFocus = { tapOffset = it },
      onSwitchCamera = { latestBitmap = it },
    )

    FocusTap(
      offset = tapOffset,
      onFocus = { onFocus { tapOffset = Offset.Zero } },
    ) { focusTapContent() }

    CameraSwitchContent(
      modifier = Modifier.fillMaxSize(),
      isCameraIdle = isCameraIdle,
      bitmap = latestBitmap,
      onPreviewStreamChanged = onPreviewStreamChanged,
      onResetBitmap = { latestBitmap = null },
      switchCameraContent = switchCameraContent,
    )

    content()
  }
}

@Composable
private fun CameraSwitchContent(
  modifier: Modifier = Modifier,
  bitmap: ImageBitmap?,
  isCameraIdle: Boolean,
  onPreviewStreamChanged: () -> Unit,
  onResetBitmap: () -> Unit,
  switchCameraContent: @Composable (ImageBitmap) -> Unit,
) {
  LaunchedEffect(isCameraIdle) {
    if (!isCameraIdle) onResetBitmap()
  }

  if (!isCameraIdle || bitmap == null) {
    return
  }

  LaunchedEffect(bitmap) { onPreviewStreamChanged() }

  switchCameraContent(bitmap)
}

@Composable
internal expect fun CameraPreviewImpl(
  modifier: Modifier = Modifier,
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
  content: @Composable () -> Unit = {},
)
