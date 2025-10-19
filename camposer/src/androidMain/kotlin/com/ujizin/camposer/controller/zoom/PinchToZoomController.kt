package com.ujizin.camposer.controller.zoom

import android.view.ScaleGestureDetector
import androidx.compose.ui.util.fastCoerceIn
import com.ujizin.camposer.session.CameraSession

internal class PinchToZoomController(
    private val cameraSession: CameraSession,
    private val onZoomRatioChanged: (Float) -> Unit,
) {

    internal fun onPinchToZoom(scaleFactor: Float): Boolean {
        if (!cameraSession.config.isPinchToZoomEnabled) return false

        val zoomRatio = (cameraSession.config.zoomRatio * scaleFactor).fastCoerceIn(
            minimumValue = cameraSession.info.minZoom,
            maximumValue = cameraSession.info.maxZoom,
        )

        onZoomRatioChanged(zoomRatio)

        return true
    }

    inner class PinchToZoomGesture() : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            return onPinchToZoom(detector.scaleFactor)
        }
    }
}