package com.ujizin.camposer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
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
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
public actual fun CameraPreview(
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
    onSwitchToFront: @Composable (ImageBitmap) -> Unit,
    onSwitchToBack: @Composable (ImageBitmap) -> Unit,
    onFocus: suspend (onComplete: () -> Unit) -> Unit,
    onZoomRatioChanged: (Float) -> Unit,
    focusTapContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
//    val previewLayer = remember(cameraState) {
//        AVCaptureVideoPreviewLayer(session = cameraState.session)
//    }

//    UIKitView(
//        modifier = modifier,
//        update = {
//            previewLayer.videoGravity = scaleType.gravity
//            cameraState.update(
//                camSelector = camSelector,
//                captureMode = captureMode,
//                scaleType = scaleType,
//                imageCaptureTargetSize = imageCaptureTargetSize,
//                isImageAnalysisEnabled = isImageAnalysisEnabled,
//                imageAnalyzer = imageAnalyzer,
//                implementationMode = implementationMode,
//                isFocusOnTapEnabled = isFocusOnTapEnabled,
//                flashMode = flashMode,
//                zoomRatio = zoomRatio,
//                imageCaptureMode = imageCaptureMode,
//                enableTorch = enableTorch,
//                exposureCompensation = exposureCompensation
//            )
//        },
//        factory = {
//            UIView().apply {
//                layer.addSublayer(previewLayer)
//                previewLayer.frame = bounds
//            }
//        }
//    )

    content()

//    DisposableEffect(cameraState) {
//        cameraState.session.startRunning()
//        onDispose { /*cameraState.session.stopRunning()*/ }
//    }
}