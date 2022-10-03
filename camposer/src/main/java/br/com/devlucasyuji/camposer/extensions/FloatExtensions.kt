package br.com.devlucasyuji.camposer.extensions

import java.util.Locale

internal fun Float.roundTo(n: Int): Float {
    return try {
        "%.${n}f".format(Locale.US, this).toFloat()
    } catch (e: NumberFormatException) {
        this
    }
}

internal fun Float.clamped(scaleFactor: Float) = this * if (scaleFactor > 1f) {
    1.0f + (scaleFactor - 1.0f) * 2
} else {
    1.0f - (1.0f - scaleFactor) * 2
}