package com.ujizin.sample.feature.camera

import android.graphics.ImageFormat.YUV_420_888
import android.graphics.ImageFormat.YUV_422_888
import android.graphics.ImageFormat.YUV_444_888
import android.os.Build
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageCaptureResult
import com.ujizin.camposer.state.VideoCaptureResult
import com.ujizin.sample.data.local.datasource.FileDataSource
import com.ujizin.sample.data.local.datasource.UserDataSource
import com.ujizin.sample.domain.User
import com.ujizin.sample.extensions.getQRCodeResult
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

    private val reader = MultiFormatReader().apply {
        val map = mapOf(DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE))
        setHints(map)
    }

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

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    toggleRecording(
                        fileDataSource.getFile("mp4"),
                        onResult = ::onVideoResult
                    )
                }
            }
        }
    }

    fun analyzeImage(image: ImageProxy) {
        viewModelScope.launch {
            if (image.format !in listOf(YUV_420_888, YUV_422_888, YUV_444_888)) {
                Log.e("QRCodeAnalyzer", "Expected YUV, now = ${image.format}")
            }
            val qrCodeResult = reader.getQRCodeResult(image)
            _uiState.update {
                CameraUiState.Ready(
                    user = user,
                    lastPicture = fileDataSource.lastPicture,
                    qrCodeText = qrCodeResult?.text
                )
            }
            image.close()
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
        val qrCodeText: String? = null,
    ) : CameraUiState
}