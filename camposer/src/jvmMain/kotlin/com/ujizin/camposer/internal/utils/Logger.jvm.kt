package com.ujizin.camposer.internal.utils

internal actual object Logger {
    actual fun d(message: String) {
        println("[Camposer] DEBUG: $message")
    }

    actual fun error(message: String, throwable: Throwable?) {
        System.err.println("[Camposer] ERROR: $message")
        throwable?.printStackTrace(System.err)
    }
}
