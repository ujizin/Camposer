package com.ujizin.camposer.manager

import com.ujizin.camposer.internal.extensions.toVideoOrientation
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravity
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplication.Companion.sharedApplication
import platform.UIKit.UIDeviceOrientationDidChangeNotification
import platform.UIKit.UIView
import platform.darwin.NSObjectProtocol

@OptIn(ExperimentalForeignApi::class)
public class PreviewManager internal constructor(
    public val videoPreviewLayer: AVCaptureVideoPreviewLayer = AVCaptureVideoPreviewLayer(),
) {

    private val notificationCenter: NSNotificationCenter by lazy {
        NSNotificationCenter.defaultCenter
    }

    private var currentGravity: AVLayerVideoGravity = videoPreviewLayer.videoGravity

    private var orientationObserver: NSObjectProtocol? = null

    internal fun start(avCaptureSession: AVCaptureSession) = with(videoPreviewLayer) {
        setSession(avCaptureSession)
    }

    internal fun attachView(view: UIView) = with(videoPreviewLayer) {
        view.layer.addSublayer(this)
        setFrame(view.bounds)

        orientationObserver = notificationCenter.addObserverForName(
            UIDeviceOrientationDidChangeNotification,
            null,
            null
        ) { updateOrientation() }

        updateOrientation()
        setVideoGravity(currentGravity)
    }

    private fun updateOrientation() = videoPreviewLayer.connection?.apply {
        videoOrientation = sharedApplication().statusBarOrientation.toVideoOrientation()
    }

    internal fun setGravity(gravity: AVLayerVideoGravity) {
        currentGravity = gravity
        videoPreviewLayer.setVideoGravity(gravity)
    }

    internal fun detachView() {
        orientationObserver?.let(notificationCenter::removeObserver)
        videoPreviewLayer.removeFromSuperlayer()
    }
}