package com.ujizin.sample.feature.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ujizin.sample.data.local.datasource.FileDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.io.File

class GalleryViewModel(fileDataSource: FileDataSource) : ViewModel() {

    private val _uiState = MutableStateFlow(
        fileDataSource.externalFiles.orEmpty().run {
            if (isEmpty()) GalleryUiState.Empty else GalleryUiState.Success(this)
        }
    ).stateIn(
        viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = GalleryUiState.Initial
    )
    val uiState: StateFlow<GalleryUiState> get() = _uiState
}

sealed interface GalleryUiState {
    object Initial : GalleryUiState
    object Empty : GalleryUiState
    data class Success(val images: List<File>) : GalleryUiState
}