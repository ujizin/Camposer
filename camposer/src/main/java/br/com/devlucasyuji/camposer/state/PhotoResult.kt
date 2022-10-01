package br.com.devlucasyuji.camposer.state

import android.net.Uri
import androidx.compose.runtime.Immutable

/**
 * Photo Result of take a picture.
 * */
sealed interface PhotoResult {
    @Immutable
    data class Success(val savedUri: Uri?) : PhotoResult

    @Immutable
    data class Error(val throwable: Throwable) : PhotoResult
}
