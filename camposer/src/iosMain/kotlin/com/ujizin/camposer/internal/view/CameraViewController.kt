package com.ujizin.camposer.internal.view

import com.ujizin.camposer.internal.view.gesture.PinchToZoomGestureHandler
import com.ujizin.camposer.internal.view.gesture.TapToFocusGestureHandler
import com.ujizin.camposer.session.CameraSession
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIViewController
import platform.darwin.dispatch_async

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class CameraViewController(
  internal val cameraSession: CameraSession,
  internal val cameraViewDelegate: CameraViewDelegate,
) : UIViewController(null, null) {
  private val tapToFocusGesture =
    TapToFocusGestureHandler(
      cameraSession = cameraSession,
      cameraViewDelegate = cameraViewDelegate,
    )

  private val pinchToZoomGesture =
    PinchToZoomGestureHandler(
      cameraSession = cameraSession,
    )

  override fun viewDidLoad() {
    super.viewDidLoad()
    addCameraGesturesRecognizer()
  }

  override fun viewWillAppear(animated: Boolean) {
    super.viewWillAppear(animated)
    observeAppLifecycle()
    cameraSession.renderCamera(view)
    dispatch_async(cameraSession.cameraQueue) {
      cameraSession.startCamera()
    }
  }

  override fun viewDidLayoutSubviews() {
    super.viewDidLayoutSubviews()
    cameraSession.renderCamera(view)
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
      `object` = null,
    )
  }

  @ObjCAction
  fun appDidBecomeActive() {
    dispatch_async(cameraSession.cameraQueue) {
      cameraSession.recoveryState()
    }
  }

  override fun viewDidDisappear(animated: Boolean) {
    NSNotificationCenter.defaultCenter.removeObserver(this)
    cameraSession.detachView()
    super.viewDidDisappear(animated)
  }
}
