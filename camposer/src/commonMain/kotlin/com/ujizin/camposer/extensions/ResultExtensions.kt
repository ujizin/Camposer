package com.ujizin.camposer.extensions

import com.ujizin.camposer.result.CaptureResult

internal fun <T> Result<T>.toCaptureResult() = when {
    isFailure -> CaptureResult.Error(exceptionOrNull()!!)
    else -> CaptureResult.Success(getOrThrow())
}
