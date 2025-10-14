package com.ujizin.camposer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitViewController
import com.ujizin.camposer.config.properties.CamSelector
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.config.properties.CaptureMode
import com.ujizin.camposer.config.properties.FlashMode
import com.ujizin.camposer.config.properties.ImageAnalyzer
import com.ujizin.camposer.config.properties.ImageCaptureStrategy
import com.ujizin.camposer.config.properties.ImplementationMode
import com.ujizin.camposer.config.properties.ResolutionPreset
import com.ujizin.camposer.config.properties.ScaleType
import com.ujizin.camposer.view.CameraViewController
import com.ujizin.camposer.view.CameraViewDelegate
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun CameraPreviewImpl(
    modifier: Modifier,
    cameraState: CameraState,
    camSelector: CamSelector,
    captureMode: CaptureMode,
    resolutionPreset: ResolutionPreset,
    imageCaptureMode: ImageCaptureStrategy,
    flashMode: FlashMode,
    scaleType: ScaleType,
    isTorchEnabled: Boolean,
    exposureCompensation: Float?,
    zoomRatio: Float,
    imageAnalyzer: ImageAnalyzer?,
    implementationMode: ImplementationMode,
    isImageAnalysisEnabled: Boolean,
    isFocusOnTapEnabled: Boolean,
    isPinchToZoomEnabled: Boolean,
    onTapFocus: (Offset) -> Unit,
    onSwitchCamera: (ImageBitmap) -> Unit,
    onZoomRatioChanged: (Float) -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current

    UIKitViewController(
        modifier = modifier,
        factory = {
            CameraViewController(
                cameraState = cameraState,
                cameraViewDelegate = object : CameraViewDelegate {
                    override fun onFocusTap(x: Float, y: Float): Unit = with(density) {
                        onTapFocus(Offset(x.dp.toPx(), y.dp.toPx()))
                    }

                    override fun onZoomChanged(zoomRatio: Float) = onZoomRatioChanged(zoomRatio)
                }
            )
        },
        update = { cameraViewController ->
            cameraViewController.update(
                camSelector = camSelector,
                captureMode = captureMode,
                scaleType = scaleType,
                isImageAnalysisEnabled = isImageAnalysisEnabled,
                imageAnalyzer = imageAnalyzer,
                implementationMode = implementationMode,
                isFocusOnTapEnabled = isFocusOnTapEnabled,
                flashMode = flashMode,
                isTorchEnabled = isTorchEnabled,
                zoomRatio = zoomRatio,
                imageCaptureMode = imageCaptureMode,
                exposureCompensation = exposureCompensation,
                isPinchToZoomEnabled = isPinchToZoomEnabled,
                resolutionPreset = resolutionPreset,
            )
        },
    )

    content()
}