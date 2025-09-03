package com.ujizin.camposer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitViewController
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.CaptureMode
import com.ujizin.camposer.state.FlashMode
import com.ujizin.camposer.state.ImageAnalyzer
import com.ujizin.camposer.state.ImageCaptureMode
import com.ujizin.camposer.state.ImageTargetSize
import com.ujizin.camposer.state.ImplementationMode
import com.ujizin.camposer.state.QualitySelector
import com.ujizin.camposer.state.ScaleType
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
    imageCaptureMode: ImageCaptureMode,
    imageCaptureTargetSize: ImageTargetSize?,
    flashMode: FlashMode,
    scaleType: ScaleType,
    enableTorch: Boolean,
    exposureCompensation: Int,
    zoomRatio: Float,
    imageAnalyzer: ImageAnalyzer?,
    implementationMode: ImplementationMode,
    videoQualitySelector: QualitySelector,
    isImageAnalysisEnabled: Boolean,
    isFocusOnTapEnabled: Boolean,
    isPinchToZoomEnabled: Boolean,
    onPreviewStreamChanged: () -> Unit,
    onTapFocus: (Offset) -> Unit,
    onSwitchCamera: (ImageBitmap) -> Unit,
    onSwitchToFront: @Composable (ImageBitmap) -> Unit,
    onSwitchToBack: @Composable (ImageBitmap) -> Unit,
    onFocus: suspend (onComplete: () -> Unit) -> Unit,
    onZoomRatioChanged: (Float) -> Unit,
    focusTapContent: @Composable () -> Unit,
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
                imageCaptureTargetSize = imageCaptureTargetSize,
                scaleType = scaleType,
                isImageAnalysisEnabled = isImageAnalysisEnabled,
                imageAnalyzer = imageAnalyzer,
                implementationMode = implementationMode,
                isFocusOnTapEnabled = isFocusOnTapEnabled,
                flashMode = flashMode,
                enableTorch = enableTorch,
                zoomRatio = zoomRatio,
                imageCaptureMode = imageCaptureMode,
                videoQualitySelector = videoQualitySelector,
                exposureCompensation = exposureCompensation,
                isPinchToZoomEnabled = isPinchToZoomEnabled,
            )
        },
    )

    content()
}