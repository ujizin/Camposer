package br.com.devlucasyuji.sample.feature.camera

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.devlucasyuji.camposer.state.CameraState
import br.com.devlucasyuji.camposer.state.ImageCaptureResult
import br.com.devlucasyuji.camposer.state.VideoCaptureResult
import br.com.devlucasyuji.sample.data.local.datasource.FileDataSource
import br.com.devlucasyuji.sample.data.local.datasource.UserDataSource
import br.com.devlucasyuji.sample.domain.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class CameraViewModel(
    private val fileDataSource: FileDataSource,
    private val userDataSource: UserDataSource,
) : ViewModel() {

    private val _uiState: MutableStateFlow<CameraUiState> = MutableStateFlow(CameraUiState.Initial)
    val uiState: StateFlow<CameraUiState> get() = _uiState

    private lateinit var user: User

    init {
        initCamera()
    }

    private fun initCamera() {
        viewModelScope.launch {
            userDataSource.getUser()
                .onStart { CameraUiState.Initial }
                .collect { user ->
                    _uiState.value = CameraUiState.Ready(user, fileDataSource.lastPicture).apply {
                        this@CameraViewModel.user = user
                    }
                }
        }
    }

    fun takePicture(cameraState: CameraState) = with(cameraState) {
        viewModelScope.launch {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> takePicture(
                    fileDataSource.imageContentValues,
                    onResult = ::onImageResult
                )

                else -> takePicture(
                    fileDataSource.getFile("jpg"),
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
                    fileDataSource.getFile("mp4"),
                    onResult = ::onVideoResult
                )
            }
        }
    }

    private fun captureSuccess() {
        viewModelScope.launch {
            _uiState.update {
                CameraUiState.Ready(user = user, lastPicture = fileDataSource.lastPicture)
            }
        }
    }

    private fun onVideoResult(videoResult: VideoCaptureResult) {
        when (videoResult) {
            is VideoCaptureResult.Error -> onError(videoResult.throwable)
            is VideoCaptureResult.Success -> captureSuccess()
        }
    }

    private fun onImageResult(imageResult: ImageCaptureResult) {
        when (imageResult) {
            is ImageCaptureResult.Error -> onError(imageResult.throwable)
            is ImageCaptureResult.Success -> captureSuccess()
        }
    }

    private fun onError(throwable: Throwable?) {
        _uiState.update { CameraUiState.Ready(user, fileDataSource.lastPicture, throwable) }
    }
}

sealed interface CameraUiState {
    object Initial : CameraUiState
    data class Ready(
        val user: User,
        val lastPicture: File?,
        val throwable: Throwable? = null,
    ) : CameraUiState
}