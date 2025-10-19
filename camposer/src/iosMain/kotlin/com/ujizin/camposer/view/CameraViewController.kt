package com.ujizin.camposer.view

import com.ujizin.camposer.state.properties.CamSelector
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.ImplementationMode
import com.ujizin.camposer.state.properties.ResolutionPreset
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.update
import com.ujizin.camposer.session.CameraSession
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
    internal val cameraession: CameraSession,
    internal val cameraViewDelegate: CameraViewDelegate,
) : UIViewController(null, null) {

    private val tapToFocusGesture = TapToFocusGestureHandler(
        cameraSession = cameraession,
        cameraViewDelegate = cameraViewDelegate,
    )

    private val pinchToZoomGesture = PinchToZoomGestureHandler(
        cameraSession = cameraession,
        cameraViewDelegate = cameraViewDelegate,
    )

    override fun viewDidLoad() {
        super.viewDidLoad()
        observeAppLifecycle()
        addCameraGesturesRecognizer()

        cameraession.startCamera()
    }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        cameraession.renderCamera(view)
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
        val hasCameraChanged = cameraession.state.camSelector != camSelector
        if (hasCameraChanged) {
            onBeforeSwitchCamera()
        }

        cameraession.state.update(
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
        cameraViewDelegate.onZoomChanged(cameraession.info.minZoom)
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
        cameraession.recoveryState()
    }

    override fun viewDidDisappear(animated: Boolean) {
        NSNotificationCenter.defaultCenter.removeObserver(this)
        cameraession.dispose()
        super.viewDidDisappear(animated)
    }
}
