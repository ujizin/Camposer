package com.ujizin.camposer.internal.extensions

import com.ujizin.camposer.CaptureResult

internal fun <T> Result<T>.toCaptureResult() = when {
    isFailure -> CaptureResult.Error(exceptionOrNull()!!)
    else -> CaptureResult.Success(getOrThrow())
}
