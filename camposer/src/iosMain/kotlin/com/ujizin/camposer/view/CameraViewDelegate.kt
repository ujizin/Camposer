package com.ujizin.camposer.view

internal interface CameraViewDelegate {
    fun onFocusTap(x: Float, y: Float)
    fun onZoomChanged(zoomRatio: Float)
}