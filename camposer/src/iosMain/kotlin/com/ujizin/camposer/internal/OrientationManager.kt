package com.ujizin.camposer.internal

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.placeTo
import kotlinx.cinterop.pointed
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIInterfaceOrientation
import platform.UIKit.UIInterfaceOrientationLandscapeLeft
import platform.UIKit.UIInterfaceOrientationLandscapeRight
import platform.UIKit.UIInterfaceOrientationPortrait
import platform.UIKit.UIInterfaceOrientationPortraitUpsideDown
import kotlin.math.abs

internal class OrientationManager {
  private val motionManager = CMMotionManager()

  private var _currentOrientation: UIInterfaceOrientation = UIInterfaceOrientationPortrait
  val currentOrientation: UIInterfaceOrientation get() = _currentOrientation

  @OptIn(ExperimentalForeignApi::class)
  fun start() {
    if (!motionManager.isDeviceMotionAvailable()) return
    motionManager.deviceMotionUpdateInterval = 0.2
    motionManager.startDeviceMotionUpdatesToQueue(NSOperationQueue.mainQueue()) { motion, _ ->
      memScoped {
        motion?.gravity?.placeTo(this)?.pointed?.let { g ->
          _currentOrientation = mapGravityToInterfaceOrientation(g.x, g.y)
        }
      }
    }
  }

  fun stop() {
    motionManager.stopDeviceMotionUpdates()
  }

  private fun mapGravityToInterfaceOrientation(
    x: Double,
    y: Double,
  ): UIInterfaceOrientation =
    when {
      abs(x) > abs(y) -> when {
        x > 0 -> UIInterfaceOrientationLandscapeLeft
        else -> UIInterfaceOrientationLandscapeRight
      }

      else -> when {
        y > 0 -> UIInterfaceOrientationPortraitUpsideDown
        else -> UIInterfaceOrientationPortrait
      }
    }
}
