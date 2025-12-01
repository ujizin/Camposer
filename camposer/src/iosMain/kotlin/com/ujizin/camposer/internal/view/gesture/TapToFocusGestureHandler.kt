package com.ujizin.camposer.internal.view.gesture

import com.ujizin.camposer.internal.view.CameraViewDelegate
import com.ujizin.camposer.session.CameraSession
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.placeTo
import kotlinx.cinterop.pointed
import platform.CoreGraphics.CGPointMake
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UITapGestureRecognizer
import platform.darwin.NSObject

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
internal class TapToFocusGestureHandler(
  private val cameraSession: CameraSession,
  private val cameraViewDelegate: CameraViewDelegate,
) : NSObject() {
  internal val recognizer: UITapGestureRecognizer = UITapGestureRecognizer(
    target = this,
    action = NSSelectorFromString("${::onTap.name}:"),
  )

  @ObjCAction
  internal fun onTap(sender: UITapGestureRecognizer) {
    if (!cameraSession.info.isFocusSupported || !cameraSession.state.isFocusOnTapEnabled) return
    memScoped {
      val view = sender.view ?: return
      val size = view.bounds
        .placeTo(this)
        .pointed.size
      val cgPoint = sender.locationInView(view).placeTo(this).pointed
      val x = cgPoint.x / size.width
      val y = cgPoint.y / size.height
      val focusPoint = CGPointMake(x = x, y = y)

      cameraViewDelegate.onFocusTap(cgPoint.x.toFloat(), cgPoint.y.toFloat())
      cameraSession.setFocusPoint(focusPoint)
    }
  }
}
