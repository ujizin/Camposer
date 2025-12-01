package com.ujizin.camposer.internal.zoom

import android.view.ScaleGestureDetector
import androidx.compose.ui.util.fastCoerceIn
import com.ujizin.camposer.session.CameraSession

internal class PinchToZoomController(
  private val cameraSession: CameraSession,
) {
  internal fun onPinchToZoom(scaleFactor: Float): Boolean {
    if (!cameraSession.state.isPinchToZoomEnabled) return false

    val zoomRatio = (cameraSession.state.zoomRatio * scaleFactor).fastCoerceIn(
      minimumValue = cameraSession.info.minZoom,
      maximumValue = cameraSession.info.maxZoom,
    )

    cameraSession.controller.setZoomRatio(zoomRatio)

    return true
  }

  inner class PinchToZoomGesture : ScaleGestureDetector.SimpleOnScaleGestureListener() {
    override fun onScale(detector: ScaleGestureDetector): Boolean =
      onPinchToZoom(detector.scaleFactor)
  }
}
