package com.ujizin.sample.feature.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ujizin.sample.data.local.datasource.FileDataSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.io.File

class GalleryViewModel(fileDataSource: FileDataSource) : ViewModel() {

    val uiState = flow {
        fileDataSource.externalFiles.orEmpty().run {
            val uiState = when {
                isEmpty() -> GalleryUiState.Empty
                else -> GalleryUiState.Success(this)
            }
            emit(uiState)
        }
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = GalleryUiState.Initial
    )
}

sealed interface GalleryUiState {
    data object Initial : GalleryUiState
    data object Empty : GalleryUiState
    data class Success(val images: List<File>) : GalleryUiState
}
