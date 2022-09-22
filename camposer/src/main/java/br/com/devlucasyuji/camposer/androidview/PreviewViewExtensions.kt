package br.com.devlucasyuji.camposer.androidview

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.camera.view.PreviewView
import androidx.compose.ui.geometry.Offset

@SuppressLint("ClickableViewAccessibility")
internal fun PreviewView.setOnTapClickListener(onTap: ((Offset) -> Unit)) {
    var isTapped = false
    setOnTouchListener { _, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_UP -> if (isTapped) onTap(Offset(event.x, event.y))
            MotionEvent.ACTION_DOWN -> isTapped = true
            else -> isTapped = false
        }

        onTouchEvent(event)
    }
}
