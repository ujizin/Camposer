package br.com.devlucasyuji.sample.feature.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.devlucasyuji.sample.feature.camera.datasource.FileDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GalleryViewModel(
    fileDataSource: FileDataSource = FileDataSource(), // TODO add DI
) : ViewModel() {

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Initial)
    val uiState: StateFlow<GalleryUiState> get() = _uiState

    init {
        viewModelScope.launch {
            val images = withContext(Dispatchers.IO) {
                fileDataSource.externalFiles.orEmpty()
            }
            _uiState.value = GalleryUiState.Success(images)
        }
    }
}

sealed interface GalleryUiState {
    object Initial : GalleryUiState
    data class Success(val images: List<File>) : GalleryUiState
}