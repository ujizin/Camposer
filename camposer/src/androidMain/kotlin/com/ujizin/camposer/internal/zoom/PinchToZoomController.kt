package com.ujizin.camposer.internal.zoom

import android.view.ScaleGestureDetector
import androidx.compose.ui.util.fastCoerceIn
import com.ujizin.camposer.session.CameraSession

internal class PinchToZoomController(
  private val cameraSession: CameraSession,
) {
  internal fun onPinchToZoom(scaleFactor: Float): Boolean {
    if (!cameraSession.state.isPinchToZoomEnabled.value ||
      !cameraSession.isInitialized
    ) {
      return false
    }

    val zoomRatio = (cameraSession.state.zoomRatio.value * scaleFactor).fastCoerceIn(
      minimumValue = cameraSession.info.state.value.minZoom,
      maximumValue = cameraSession.info.state.value.maxZoom,
    )

    cameraSession.controller.setZoomRatio(zoomRatio)

    return true
  }

  inner class PinchToZoomGesture : ScaleGestureDetector.SimpleOnScaleGestureListener() {
    override fun onScale(detector: ScaleGestureDetector): Boolean =
      onPinchToZoom(detector.scaleFactor)
  }
}
