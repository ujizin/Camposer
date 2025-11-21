package com.ujizin.camposer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitViewController
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.ImplementationMode
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.selector.CamSelector
import com.ujizin.camposer.state.update
import com.ujizin.camposer.view.CameraViewController
import com.ujizin.camposer.view.CameraViewDelegate
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
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
    onTapFocus: (Offset) -> Unit,
    onSwitchCamera: (ImageBitmap) -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current

    UIKitViewController(
        modifier = modifier,
        factory = {
            CameraViewController(
                cameraSession = cameraSession,
                cameraViewDelegate = object : CameraViewDelegate {
                    override fun onFocusTap(x: Float, y: Float): Unit = with(density) {
                        onTapFocus(Offset(x.dp.toPx(), y.dp.toPx()))
                    }
                }
            )
        },
        update = { cameraViewController ->
            cameraViewController.cameraSession.update(
                camSelector = camSelector,
                captureMode = captureMode,
                camFormat = camFormat,
                scaleType = scaleType,
                isImageAnalysisEnabled = isImageAnalysisEnabled,
                imageAnalyzer = imageAnalyzer,
                implementationMode = implementationMode,
                isFocusOnTapEnabled = isFocusOnTapEnabled,
                imageCaptureStrategy = imageCaptureStrategy,
                isPinchToZoomEnabled = isPinchToZoomEnabled,
            )
            cameraViewController.cameraSession.onSessionStarted()
        },
    )

    content()
}