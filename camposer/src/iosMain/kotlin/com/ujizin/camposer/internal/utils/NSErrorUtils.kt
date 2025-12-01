@file:OptIn(ExperimentalForeignApi::class)

package com.ujizin.camposer.internal.utils

import com.ujizin.camposer.internal.error.NSErrorException
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSError

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Throws(NSErrorException::class)
internal fun <T> executeWithErrorHandling(
  operation: (errorPtr: CPointer<ObjCObjectVar<NSError?>>) -> T,
): T {
  memScoped {
    val errorPtr: CPointer<ObjCObjectVar<NSError?>> = alloc<ObjCObjectVar<NSError?>>().ptr
    val result: T = operation(errorPtr)
    val error: NSError? = errorPtr.pointed.value
    if (error != null) {
      throw NSErrorException(error)
    }
    return result
  }
}
