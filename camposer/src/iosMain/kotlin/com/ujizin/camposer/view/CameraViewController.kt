package com.ujizin.camposer.view

import com.ujizin.camposer.config.properties.CamSelector
import com.ujizin.camposer.config.properties.CaptureMode
import com.ujizin.camposer.config.properties.FlashMode
import com.ujizin.camposer.config.properties.ImageAnalyzer
import com.ujizin.camposer.config.properties.ImageCaptureStrategy
import com.ujizin.camposer.config.properties.ImplementationMode
import com.ujizin.camposer.config.properties.ResolutionPreset
import com.ujizin.camposer.config.properties.ScaleType
import com.ujizin.camposer.config.update
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.view.gesture.PinchToZoomGestureHandler
import com.ujizin.camposer.view.gesture.TapToFocusGestureHandler
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class CameraViewController(
    internal val cameraState: CameraState,
    internal val cameraViewDelegate: CameraViewDelegate,
) : UIViewController(null, null) {

    private val tapToFocusGesture = TapToFocusGestureHandler(
        cameraState = cameraState,
        cameraViewDelegate = cameraViewDelegate,
    )

    private val pinchToZoomGesture = PinchToZoomGestureHandler(
        cameraState = cameraState,
        cameraViewDelegate = cameraViewDelegate,
    )

    override fun viewDidLoad() {
        super.viewDidLoad()
        observeAppLifecycle()
        addCameraGesturesRecognizer()

        cameraState.startCamera(view)
    }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        cameraState.renderCamera(view)
    }

    internal fun update(
        camSelector: CamSelector,
        captureMode: CaptureMode,
        scaleType: ScaleType,
        isImageAnalysisEnabled: Boolean,
        imageAnalyzer: ImageAnalyzer?,
        implementationMode: ImplementationMode,
        isFocusOnTapEnabled: Boolean,
        flashMode: FlashMode,
        zoomRatio: Float,
        imageCaptureMode: ImageCaptureStrategy,
        isTorchEnabled: Boolean,
        exposureCompensation: Float?,
        isPinchToZoomEnabled: Boolean,
        resolutionPreset: ResolutionPreset,
    ) {
        val hasCameraChanged = cameraState.config.camSelector != camSelector
        if (hasCameraChanged) {
            onBeforeSwitchCamera()
        }

        cameraState.config.update(
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
            imageCaptureStrategy = imageCaptureMode,
            resolutionPreset = resolutionPreset,
            exposureCompensation = exposureCompensation,
            isPinchToZoomEnabled = isPinchToZoomEnabled,
        )
    }

    // FIXME this might not be need when preview stream initialized be implemented
    private fun onBeforeSwitchCamera() {
        cameraViewDelegate.onZoomChanged(cameraState.info.minZoom)
    }

    private fun addCameraGesturesRecognizer() {
        view.addGestureRecognizer(tapToFocusGesture.recognizer)
        view.addGestureRecognizer(pinchToZoomGesture.recognizer)
    }

    private fun observeAppLifecycle() {
        val notificationCenter = NSNotificationCenter.defaultCenter
        notificationCenter.addObserver(
            observer = this,
            selector = NSSelectorFromString(::appDidBecomeActive.name),
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null
        )
    }

    @ObjCAction
    fun appDidBecomeActive() {
        cameraState.recoveryState()
    }

    override fun viewDidDisappear(animated: Boolean) {
        NSNotificationCenter.defaultCenter.removeObserver(this)
        cameraState.dispose()
        super.viewDidDisappear(animated)
    }
}
