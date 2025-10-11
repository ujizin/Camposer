package com.ujizin.camposer.view

import com.ujizin.camposer.config.update
import com.ujizin.camposer.config.properties.CamSelector
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.config.properties.CaptureMode
import com.ujizin.camposer.config.properties.FlashMode
import com.ujizin.camposer.config.properties.ImageAnalyzer
import com.ujizin.camposer.config.properties.ImageCaptureStrategy
import com.ujizin.camposer.config.properties.ImplementationMode
import com.ujizin.camposer.config.properties.ResolutionPreset
import com.ujizin.camposer.config.properties.ScaleType
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.placeTo
import kotlinx.cinterop.pointed
import platform.CoreGraphics.CGPointMake
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIGestureRecognizerStateChanged
import platform.UIKit.UIPinchGestureRecognizer
import platform.UIKit.UITapGestureRecognizer
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class CameraViewController(
    internal val cameraState: CameraState,
    internal val cameraViewDelegate: CameraViewDelegate,
) : UIViewController(null, null) {

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

    // TODO refactor to new class
    @ObjCAction
    private fun onTap(sender: UITapGestureRecognizer) {
        if (!cameraState.config.isFocusOnTapEnabled) return
        memScoped {
            val size = view.bounds.placeTo(this).pointed.size
            val cgPoint = sender.locationInView(view).placeTo(this).pointed
            val x = cgPoint.y / size.height
            val y = 1 - cgPoint.x / size.width
            val focusPoint = CGPointMake(x = x, y = y)

            cameraViewDelegate.onFocusTap(cgPoint.x.toFloat(), cgPoint.y.toFloat())
            cameraState.setFocusPoint(focusPoint)
        }
    }

    // TODO refactor to new class
    @ObjCAction
    private fun onPinch(sender: UIPinchGestureRecognizer) {
        if (!cameraState.config.isPinchToZoomEnabled || sender.state != UIGestureRecognizerStateChanged) return
        memScoped {
            val scale = sender.scale.toFloat()
            val clampedZoom = (cameraState.config.zoomRatio * scale).coerceIn(
                minimumValue = cameraState.info.minZoom,
                maximumValue = cameraState.info.maxZoom,
            )
            cameraViewDelegate.onZoomChanged(clampedZoom)
            sender.scale = 1.0
        }
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

    private fun onBeforeSwitchCamera() {
        cameraViewDelegate.onZoomChanged(cameraState.info.minZoom)
    }

    private fun addCameraGesturesRecognizer() {
        view.addGestureRecognizer(
            UITapGestureRecognizer(
                target = this, action = NSSelectorFromString("onTap:"),
            )
        )
        view.addGestureRecognizer(
            UIPinchGestureRecognizer(
                target = this,
                action = NSSelectorFromString("onPinch:")
            )
        )
    }

    private fun observeAppLifecycle() {
        val notificationCenter = NSNotificationCenter.defaultCenter
        notificationCenter.addObserver(
            observer = this,
            selector = NSSelectorFromString("appDidBecomeActive"),
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
