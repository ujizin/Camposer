package br.com.devlucasyuji.sample.feature.gallery

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

class GalleryViewModel : ViewModel() {

    private val externalStorage = Environment.getExternalStoragePublicDirectory(
        "Pictures/Camposer"
    ).listFiles()

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Initial)
    val uiState: StateFlow<GalleryUiState> get() = _uiState

    private val File.videoCompat: File?
        get() = if (extension == ".mp4") {
            MediaMetadataRetriever().run {
                setDataSource(this@videoCompat.path)
                val bitmap = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> getFrameAtIndex(0)
                    else -> getFrameAtTime(0)
                }
                val stream = ByteArrayOutputStream().apply {
                    bitmap?.compress(
                        Bitmap.CompressFormat.JPEG,
                        100,
                        this
                    )
                }
                bitmap?.recycle()

                File.createTempFile("video", "jpg").apply { writeBytes(stream.toByteArray()) }
            }
        } else this

    private val File.extension: String
        get() = path.substring(path.lastIndexOf("."))

    init {
        viewModelScope.launch {
            val images = withContext(Dispatchers.IO) {
                externalStorage?.reversed()?.map { it.videoCompat } ?: listOf()
            }
            _uiState.value = GalleryUiState.Success(images)
        }
    }
}

sealed interface GalleryUiState {
    object Initial : GalleryUiState
    data class Success(val images: List<File?>) : GalleryUiState
}