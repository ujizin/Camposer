package com.ujizin.camposer.extensions

internal fun Float.clamped(scaleFactor: Float) = this * if (scaleFactor > 1f) {
    1.0f + (scaleFactor - 1.0f) * 2
} else {
    1.0f - (1.0f - scaleFactor) * 2
}