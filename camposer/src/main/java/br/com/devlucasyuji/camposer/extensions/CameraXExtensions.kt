package br.com.devlucasyuji.camposer.extensions

import androidx.camera.core.ZoomState

internal val ZoomState.roundedZoomRatio get() = zoomRatio.roundTo(1)
