package com.ujizin.camposer.extensions

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewConfiguration.getLongPressTimeout
import androidx.camera.view.PreviewView
import androidx.compose.ui.geometry.Offset
import com.ujizin.camposer.controller.zoom.PinchToZoomController


@SuppressLint("ClickableViewAccessibility")
internal fun PreviewView.setCameraTouchEvent(
    pinchZoomController: PinchToZoomController,
    onTap: (Offset) -> Unit,
) {
    val scaleGesture = ScaleGestureDetector(
        /*context = */context,
        /*listener = */ pinchZoomController.PinchToZoomGesture(),
    )

    setOnTouchListener { _, event ->
        val isSingleTouch = event.pointerCount == 1
        val isUpEvent = event.action == MotionEvent.ACTION_UP
        val notALongPress = (event.eventTime - event.downTime) < getLongPressTimeout()
        if (isSingleTouch && isUpEvent && notALongPress) {
            onTap(Offset(event.x, event.y))
        }

        scaleGesture.onTouchEvent(event) && onTouchEvent(event)
    }
}