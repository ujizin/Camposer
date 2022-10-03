package br.com.devlucasyuji.camposer.androidview

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import androidx.camera.view.PreviewView
import androidx.compose.ui.geometry.Offset

@SuppressLint("ClickableViewAccessibility")
internal fun PreviewView.onCameraTouchEvent(
    onTap: (Offset) -> Unit,
    onScaleChanged: (Float) -> Unit,
) {
    var isTapped = false
    val scaleGesture = ScaleGestureDetector(context, PinchToZoomGesture(onScaleChanged))
    setOnTouchListener { _, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_UP -> if (isTapped) onTap(Offset(event.x, event.y))
            MotionEvent.ACTION_DOWN -> isTapped = true
            else -> isTapped = false
        }

        scaleGesture.onTouchEvent(event) || onTouchEvent(event)
    }
}

class PinchToZoomGesture(
    private val onZoomChanged: (Float) -> Unit
) : SimpleOnScaleGestureListener() {
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        onZoomChanged(detector.scaleFactor)
        return true
    }
}