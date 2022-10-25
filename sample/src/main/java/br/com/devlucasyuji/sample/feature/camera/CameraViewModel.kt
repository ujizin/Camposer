package br.com.devlucasyuji.sample.feature.camera

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.devlucasyuji.camposer.state.CameraState
import br.com.devlucasyuji.camposer.state.ImageCaptureResult
import br.com.devlucasyuji.camposer.state.VideoCaptureResult
import br.com.devlucasyuji.sample.feature.camera.datasource.FileDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class CamposerViewModel(
    private val fileDataSource: FileDataSource = FileDataSource(),
) : ViewModel() {

    private val _uiState: MutableStateFlow<CameraUiState> = MutableStateFlow(CameraUiState.Initial)
    val uiState: StateFlow<CameraUiState> get() = _uiState

    init {
        _uiState.value = CameraUiState.Ready(fileDataSource.lastPicture)
    }

    private fun onImageResult(imageResult: ImageCaptureResult) {
        if (imageResult is ImageCaptureResult.Success) captureSuccess()
    }

    private fun captureSuccess() {
        viewModelScope.launch {
            _uiState.emit(CameraUiState.CaptureSuccess)
            delay(CAPTURED_PHOTO_DELAY)
            _uiState.emit(CameraUiState.Ready(fileDataSource.lastPicture))
        }
    }

    private fun onVideoResult(videoResult: VideoCaptureResult) {
        if (videoResult is VideoCaptureResult.Success) captureSuccess()
    }

    fun takePicture(cameraState: CameraState) = with(cameraState) {
        viewModelScope.launch {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> takePicture(
                    fileDataSource.imageContentValues,
                    onResult = ::onImageResult
                )

                else -> takePicture(
                    fileDataSource.getFile(".jpg"),
                    ::onImageResult
                )
            }
        }
    }

    fun toggleRecording(cameraState: CameraState) = with(cameraState) {
        viewModelScope.launch {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> toggleRecording(
                    fileDataSource.videoContentValues,
                    onResult = ::onVideoResult
                )

                else -> toggleRecording(
                    fileDataSource.getFile(".mp4"),
                    onResult = ::onVideoResult
                )
            }
        }
    }

    companion object {
        private const val CAPTURED_PHOTO_DELAY = 25L
    }
}

sealed interface CameraUiState {
    object Initial : CameraUiState
    data class Ready(val lastPicture: File?) : CameraUiState
    object CaptureSuccess : CameraUiState
}