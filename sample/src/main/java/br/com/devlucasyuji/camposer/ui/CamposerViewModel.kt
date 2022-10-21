package br.com.devlucasyuji.camposer.ui

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.devlucasyuji.camposer.state.ImageCaptureResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class CamposerViewModel : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> get() = _uiState

    private val currentName: String
        get() = SimpleDateFormat(
            DEFAULT_DATE_FORMAT, Locale.US
        ).format(System.currentTimeMillis())

    val imageContentValues: ContentValues = getContentValues(
        MediaStore.Images.Media.RELATIVE_PATH,
        JPEG_MIME_TYPE
    )

    val videoContentValues: ContentValues = getContentValues(
        MediaStore.Video.Media.RELATIVE_PATH,
        VIDEO_MIME_TYPE
    )

    private fun getContentValues(relativePath: String, mimeType: String) = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, currentName)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(relativePath, RELATIVE_PATH)
        }
    }

    fun onImageResult(captureResult: ImageCaptureResult) {
        viewModelScope.launch {
            if (captureResult is ImageCaptureResult.Success) {
                _uiState.emit(UiState.CaptureSuccess)
                delay(CAPTURED_PHOTO_DELAY)
                _uiState.emit(UiState.Initial)
            }
        }
    }

    companion object {
        private const val DEFAULT_DATE_FORMAT = "YYYY-HH:MM:SS"
        private const val JPEG_MIME_TYPE = "image/jpeg"
        private const val VIDEO_MIME_TYPE = "video/mp4"
        private const val RELATIVE_PATH = "Pictures/Camposer"

        private const val CAPTURED_PHOTO_DELAY = 50L
    }
}

sealed interface UiState {
    object Initial : UiState
    object CaptureSuccess : UiState
}