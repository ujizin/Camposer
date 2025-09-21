package com.ujizin.camposer.state

import android.view.ScaleGestureDetector
import com.ujizin.camposer.controller.PinchToZoomController

internal class PinchToZoomGesture(
    private val pinchZoomController: PinchToZoomController,
) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        return pinchZoomController.onPinchToZoom(detector.scaleFactor)
    }
}