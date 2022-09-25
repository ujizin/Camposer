package br.com.devlucasyuji.camposer

import androidx.compose.runtime.Immutable

/**
 * Photo Result of take a picture.
 * */
sealed interface PhotoResult {
    object Success : PhotoResult

    @Immutable
    data class Error(val throwable: Throwable) : PhotoResult
}
