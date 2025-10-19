package com.ujizin.camposer.view.gesture

import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.view.CameraViewDelegate
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.memScoped
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIGestureRecognizerStateChanged
import platform.UIKit.UIPinchGestureRecognizer
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
internal class PinchToZoomGestureHandler(
    private val cameraSession: CameraSession,
    private val cameraViewDelegate: CameraViewDelegate,
) : NSObject() {

    internal val recognizer = UIPinchGestureRecognizer(
        target = this,
        action= NSSelectorFromString("${::onPinch.name}:")
    )

    @OptIn(BetaInteropApi::class)
    @ObjCAction
    internal fun onPinch(sender: UIPinchGestureRecognizer) {
        if (!cameraSession.state.isPinchToZoomEnabled || sender.state != UIGestureRecognizerStateChanged) return
        memScoped {
            val scale = sender.scale.toFloat()
            val clampedZoom = (cameraSession.state.zoomRatio * scale).coerceIn(
                minimumValue = cameraSession.info.minZoom,
                maximumValue = cameraSession.info.maxZoom,
            )

            cameraViewDelegate.onZoomChanged(clampedZoom)
            sender.scale = 1.0
        }
    }
}
