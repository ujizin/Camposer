package com.ujizin.camposer.view.gesture

import com.ujizin.camposer.state.CameraState
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
    private val cameraState: CameraState,
    private val cameraViewDelegate: CameraViewDelegate,
) : NSObject() {

    internal val recognizer = UIPinchGestureRecognizer(
        target = this,
        action= NSSelectorFromString("${::onPinch.name}:")
    )

    @OptIn(BetaInteropApi::class)
    @ObjCAction
    internal fun onPinch(sender: UIPinchGestureRecognizer) {
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
}
