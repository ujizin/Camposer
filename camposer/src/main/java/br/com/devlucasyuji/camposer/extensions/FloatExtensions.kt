package br.com.devlucasyuji.camposer.extensions

internal fun Float.roundTo(n: Int): Float {
    return "%.${n}f".format(this).toFloat()
}
