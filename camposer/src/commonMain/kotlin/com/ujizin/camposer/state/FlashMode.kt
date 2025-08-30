package com.ujizin.camposer.state

public expect enum class FlashMode {
    On, Auto, Off;
}

/**
 * Inverse flash mode. Works only with default Off & On flash modes.
 * */
public val FlashMode.inverse: FlashMode
    get() = when(this) {
        FlashMode.On -> FlashMode.Off
        else -> FlashMode.On
    }