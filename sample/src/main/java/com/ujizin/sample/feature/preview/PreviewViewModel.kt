package com.ujizin.sample.feature.preview

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ujizin.sample.extensions.delete
import com.ujizin.sample.extensions.isVideo
import com.ujizin.sample.router.Args
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.File


class PreviewViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _uiState = MutableStateFlow<PreviewUiState>(PreviewUiState.Initial)
    val uiState: StateFlow<PreviewUiState> = _uiState

    init {
        viewModelScope.launch {
            flow {
                val path =
                    savedStateHandle.get<String?>(Args.Path) ?: return@flow emit(PreviewUiState.Empty)
                val file = File(Uri.decode(path))
                emit(PreviewUiState.Ready(file, file.isVideo))
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun deleteFile(
        context: Context,
        intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>,
        file: File
    ) {
        viewModelScope.launch {
            file.delete(context.contentResolver, intentSenderLauncher)
            if (!file.exists()) {
                _uiState.value = PreviewUiState.Deleted
            }
        }
    }
}

sealed interface PreviewUiState {
    object Initial : PreviewUiState
    object Empty : PreviewUiState
    object Deleted : PreviewUiState

    data class Ready(val file: File, val isVideo: Boolean) : PreviewUiState
}