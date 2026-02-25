package com.ujizin.sample.feature.preview

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ujizin.sample.extensions.delete
import com.ujizin.sample.extensions.isVideo
import com.ujizin.sample.router.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class PreviewViewModel(
  savedStateHandle: SavedStateHandle,
) : ViewModel() {
  private val _uiState = MutableStateFlow<PreviewUiState>(PreviewUiState.Initial)
  val uiState: StateFlow<PreviewUiState> = _uiState

  private val route = savedStateHandle.toRoute<Router.Preview>()

  init {
    viewModelScope.launch {
      flow {
        val file = File(Uri.decode(route.path))
        emit(PreviewUiState.Ready(file, file.isVideo))
      }.collect { state ->
        _uiState.update { state }
      }
    }
  }

  fun deleteFile(
    context: Context,
    intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>,
    file: File,
  ) {
    viewModelScope.launch {
      file.delete(context.contentResolver, intentSenderLauncher)
      if (!file.exists()) {
        _uiState.update { PreviewUiState.Deleted }
      }
    }
  }
}

sealed interface PreviewUiState {
  data object Initial : PreviewUiState

  data object Empty : PreviewUiState

  data object Deleted : PreviewUiState

  data class Ready(
    val file: File,
    val isVideo: Boolean,
  ) : PreviewUiState
}
