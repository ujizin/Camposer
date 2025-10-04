package com.ujizin.camposer.controller.zoom

import android.view.ScaleGestureDetector
import androidx.compose.ui.util.fastCoerceIn
import com.ujizin.camposer.state.CameraState

internal class PinchToZoomController(
    private val cameraState: CameraState,
    private var zoomRatio: Float,
    private val onZoomRatioChanged: (Float) -> Unit,
) {

    internal fun onPinchToZoom(scaleFactor: Float): Boolean {
        if (!cameraState.isPinchToZoomEnabled) return false

        zoomRatio = (zoomRatio * scaleFactor).fastCoerceIn(
            minimumValue = cameraState.minZoom,
            maximumValue = cameraState.maxZoom,
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