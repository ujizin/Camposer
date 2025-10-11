package com.ujizin.camposer.controller.zoom

import android.view.ScaleGestureDetector
import androidx.compose.ui.util.fastCoerceIn
import com.ujizin.camposer.state.CameraState

internal class PinchToZoomController(
    private val cameraState: CameraState,
    private val onZoomRatioChanged: (Float) -> Unit,
) {

    internal fun onPinchToZoom(scaleFactor: Float): Boolean {
        if (!cameraState.config.isPinchToZoomEnabled) return false

        val zoomRatio = (cameraState.config.zoomRatio * scaleFactor).fastCoerceIn(
            minimumValue = cameraState.info.minZoom,
            maximumValue = cameraState.info.maxZoom,
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