package br.com.devlucasyuji.sample.feature.preview

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.devlucasyuji.sample.router.Args
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.io.File

class PreviewViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val uiState: StateFlow<PreviewUiState> = flow {
        val path = savedStateHandle.get<String?>(Args.Path)
            ?: return@flow emit(PreviewUiState.Empty)
        val file = File(Uri.decode(path))
        val previewUiState = when (file.extension) {
            "mp4" -> PreviewUiState.Video(file)
            else -> PreviewUiState.Image(file)
        }
        emit(previewUiState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PreviewUiState.Initial
    )
}

sealed interface PreviewUiState {
    object Initial : PreviewUiState
    object Empty : PreviewUiState
    data class Image(val file: File) : PreviewUiState
    data class Video(val file: File) : PreviewUiState
}