package com.ujizin.camposer

import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ujizin.camposer.extensions.setCameraTouchEvent
import com.ujizin.camposer.internal.observeAsState
import com.ujizin.camposer.internal.zoom.PinchToZoomController
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.ImplementationMode
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.selector.CamSelector
import com.ujizin.camposer.state.properties.type
import com.ujizin.camposer.state.properties.value
import com.ujizin.camposer.state.update

/**
 * Creates a Camera Preview's composable.
 *
 * @param cameraSession camera state hold some states and camera's controller
 * @param camSelector camera selector to be added, default is back
 * @param captureMode camera capture mode, default is image
 * @param imageCaptureStrategy camera image capture mode, default is minimum latency for better performance
 * @param scaleType scale type to be added, default is fill center
 * @param imageAnalyzer image analyzer from camera, see [ImageAnalyzer]
 * @param implementationMode implementation mode to be added, default is performance
 * @param isImageAnalysisEnabled enable or disable image analysis
 * @param isFocusOnTapEnabled turn on feature focus on tap if true
 * @param isPinchToZoomEnabled turn on feature pinch to zoom if true
 * @param content content composable within of camera preview.
 * @see ImageAnalyzer
 * @see CameraSession
 * */
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
  content: @Composable (() -> Unit),
) {
  val backgroundColor = remember(previewBackgroundColor) { previewBackgroundColor.toArgb() }
  val lifecycleOwner = LocalLifecycleOwner.current
  val lifecycleEvent by lifecycleOwner.lifecycle.observeAsState()
  val cameraIsInitialized by rememberUpdatedState(cameraSession.isInitialized)
  val isCameraIdle by rememberUpdatedState(!cameraSession.isStreaming)
  var latestBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
  var cameraOffset by remember { mutableStateOf(Offset.Zero) }

  LaunchedEffect(latestBitmap) { latestBitmap?.let(onSwitchCamera) }

  LaunchedEffect(cameraSession) {
    val previewView = cameraSession.previewView ?: return@LaunchedEffect
    if (cameraSession.cameraXControllerWrapper.isCameraControllerEquals(
        previewView.controller,
      )
    ) {
      return@LaunchedEffect
    }

    previewView.onViewBind(
      cameraSession = cameraSession,
      lifecycleOwner = lifecycleOwner,
      onTapFocus = {
        val cameraInfoState = cameraSession.info.state.value
        if (cameraInfoState.isFocusSupported &&
          cameraSession.state.isFocusOnTapEnabled.value
        ) {
          onTapFocus(it + cameraOffset)
        }
      },
    )
  }

  AndroidView(
    modifier = modifier.onGloballyPositioned { cameraOffset = it.positionInParent() },
    factory = {
      PreviewView(it).apply {
        cameraSession.previewView = this
        this.scaleType = scaleType.type
        this.implementationMode = implementationMode.value
      }
    },
    update = { previewView ->
      if (!cameraIsInitialized) return@AndroidView
      with(previewView) {
        if (this.scaleType != scaleType.type ||
          this.implementationMode != implementationMode.value
        ) {
          this.scaleType = scaleType.type
          this.implementationMode = implementationMode.value
          cameraSession.rebind(lifecycleOwner)
        }

        previewView.updateBackgroundColor(backgroundColor)

        latestBitmap = getLastBitmapOnSwitch(
          cameraSession = cameraSession,
          camSelector = camSelector,
          isCameraIdle = isCameraIdle,
          lifecycleEvent = lifecycleEvent,
          latestBitmap = latestBitmap,
        )

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
    },
  )

  content()
}

private fun PreviewView.getLastBitmapOnSwitch(
  cameraSession: CameraSession,
  camSelector: CamSelector,
  isCameraIdle: Boolean,
  lifecycleEvent: Lifecycle.Event,
  latestBitmap: ImageBitmap?,
): ImageBitmap? =
  when {
    lifecycleEvent == Lifecycle.Event.ON_STOP -> {
      null
    }

    !isCameraIdle && camSelector != cameraSession.state.camSelector.value -> {
      bitmap?.asImageBitmap()
    }

    else -> {
      latestBitmap
    }
  }

private fun PreviewView.updateBackgroundColor(backgroundColor: Int) {
  val previewBackground = background
  if (previewBackground is ColorDrawable && previewBackground.color != backgroundColor) {
    setBackgroundColor(backgroundColor)
  }
}

private fun PreviewView.onViewBind(
  cameraSession: CameraSession,
  lifecycleOwner: LifecycleOwner,
  onTapFocus: (Offset) -> Unit,
) {
  layoutParams = ViewGroup.LayoutParams(
    ViewGroup.LayoutParams.MATCH_PARENT,
    ViewGroup.LayoutParams.MATCH_PARENT,
  )
  cameraSession.cameraXControllerWrapper.apply {
    bindToLifecycle(lifecycleOwner)
    attachPreview(this@onViewBind)
  }

  previewStreamState.observe(lifecycleOwner) { state ->
    cameraSession.isStreaming = state == PreviewView.StreamState.STREAMING
  }

  setCameraTouchEvent(
    pinchZoomController = PinchToZoomController(cameraSession = cameraSession),
    onTap = onTapFocus,
  )
}
