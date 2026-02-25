package com.ujizin.camposer.internal.view.gesture

import com.ujizin.camposer.session.CameraSession
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
) : NSObject() {
  internal val recognizer = UIPinchGestureRecognizer(
    target = this,
    action = NSSelectorFromString("${::onPinch.name}:"),
  )

  @OptIn(BetaInteropApi::class)
  @ObjCAction
  internal fun onPinch(sender: UIPinchGestureRecognizer) {
    if (!cameraSession.state.isPinchToZoomEnabled.value ||
      sender.state != UIGestureRecognizerStateChanged
    ) {
      return
    }
    memScoped {
      val scale = sender.scale.toFloat()
      val cameraInfoState = cameraSession.info.state.value
      val clampedZoom = (cameraSession.state.zoomRatio.value * scale).coerceIn(
        minimumValue = cameraInfoState.minZoom,
        maximumValue = cameraInfoState.maxZoom,
      )

      cameraSession.controller.setZoomRatio(clampedZoom)
      sender.scale = 1.0
    }
  }
}
