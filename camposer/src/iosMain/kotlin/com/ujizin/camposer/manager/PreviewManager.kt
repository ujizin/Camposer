package com.ujizin.camposer.manager

import com.ujizin.camposer.extensions.toVideoOrientation
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravity
import platform.UIKit.UIDevice
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
public class PreviewManager internal constructor(
    public val videoPreviewLayer: AVCaptureVideoPreviewLayer = AVCaptureVideoPreviewLayer(),
) {

    private var currentGravity: AVLayerVideoGravity = videoPreviewLayer.videoGravity

    internal fun start(avCaptureSession: AVCaptureSession) = with(videoPreviewLayer) {
        setSession(avCaptureSession)
    }

    internal fun attachView(view: UIView) = with(videoPreviewLayer) {
        view.layer.addSublayer(this)
        setFrame(view.bounds)
        connection?.videoOrientation = UIDevice.currentDevice.orientation.toVideoOrientation()
        setVideoGravity(currentGravity)
    }

    internal fun setGravity(gravity: AVLayerVideoGravity) {
        currentGravity = gravity
        videoPreviewLayer.setVideoGravity(gravity)
    }

    internal fun detachView() {
        videoPreviewLayer.removeFromSuperlayer()
    }
}