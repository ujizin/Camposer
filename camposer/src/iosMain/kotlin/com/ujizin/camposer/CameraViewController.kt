package com.ujizin.camposer

import com.ujizin.camposer.state.CameraState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.placeTo
import kotlinx.cinterop.pointed
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeLeft
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoOrientationPortraitUpsideDown
import platform.CoreGraphics.CGPointMake
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation
import platform.UIKit.UIEvent
import platform.UIKit.UITouch
import platform.UIKit.UIViewController

internal interface CameraViewDelegate {
    fun onFocusTap(x: Float, y: Float)
}

@OptIn(ExperimentalForeignApi::class)
internal class CameraViewController(
    internal val cameraState: CameraState,
    internal val cameraViewDelegate: CameraViewDelegate,
) : UIViewController(null, null) {

    override fun viewDidLoad() {
        super.viewDidLoad()
        cameraState.initCamera(view)
        cameraState.startCamera()
    }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        cameraState.previewLayer?.apply {
            setFrame(view.bounds)
            connection?.videoOrientation = currentVideoOrientation()
        }
    }

    override fun touchesBegan(
        touches: Set<*>,
        withEvent: UIEvent?
    ) {
        super.touchesBegan(touches, withEvent)

        val touchPoint = touches.firstOrNull() as? UITouch ?: return
        onTap(touchPoint)
    }

    private fun onTap(touchPoint: UITouch) {
        if (!cameraState.isFocusOnTapSupported || !cameraState.isFocusOnTapEnabled) return
        memScoped {
            val size = view.bounds.placeTo(this).pointed.size
            val cgPoint = touchPoint.locationInView(view).placeTo(this).pointed
            val x = cgPoint.y / size.height
            val y = 1 - cgPoint.x / size.width
            val focusPoint = CGPointMake(x = x, y = y)

            cameraViewDelegate.onFocusTap(cgPoint.x.toFloat(), cgPoint.y.toFloat())
            cameraState.onTapFocus(focusPoint)
        }
    }

    private fun currentVideoOrientation(): AVCaptureVideoOrientation {
        val orientation = UIDevice.currentDevice.orientation
        return when (orientation) {
            UIDeviceOrientation.UIDeviceOrientationPortrait -> AVCaptureVideoOrientationPortrait
            UIDeviceOrientation.UIDeviceOrientationPortraitUpsideDown -> AVCaptureVideoOrientationPortraitUpsideDown
            UIDeviceOrientation.UIDeviceOrientationLandscapeLeft -> AVCaptureVideoOrientationLandscapeRight
            UIDeviceOrientation.UIDeviceOrientationLandscapeRight -> AVCaptureVideoOrientationLandscapeLeft
            else -> AVCaptureVideoOrientationPortrait
        }
    }

    override fun viewDidDisappear(animated: Boolean) {
        cameraState.dispose()
        super.viewDidDisappear(animated)
    }
}