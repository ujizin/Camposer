package com.ujizin.camposer.shared.features.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionState.Granted
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.camera.CAMERA
import dev.icerock.moko.permissions.microphone.RECORD_AUDIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PermissionsViewModel(
  private val controller: PermissionsController,
) : ViewModel() {

  private val _uiState = MutableStateFlow(PermissionsUiState())
  val uiState = _uiState.asStateFlow()

  fun onResume() {
    viewModelScope.launch {
      _uiState.update { state ->
        state.copy(
          isCameraGranted = controller.getPermissionState(Permission.CAMERA) == Granted,
          isRecordAudioGranted = controller.getPermissionState(Permission.RECORD_AUDIO) == Granted,
          isLoading = false
        )
      }
    }
  }

  fun provideRecordAudioPermission() {
    viewModelScope.launch {
      val permissionState = providePermission(Permission.RECORD_AUDIO)
      _uiState.update { state -> state.copy(isRecordAudioGranted = permissionState == Granted) }
    }
  }

  fun provideCameraPermission() {
    viewModelScope.launch {
      val permissionState = providePermission(Permission.CAMERA)
      _uiState.update { state -> state.copy(isCameraGranted = permissionState == Granted) }
    }
  }

  private suspend fun providePermission(
    permission: Permission,
  ) = suspendCancellableCoroutine { cont ->
    CoroutineScope(cont.context).launch {
      try {
        controller.providePermission(permission)
        cont.resume(Granted)
      } catch (e: DeniedAlwaysException) {
        cont.resume(PermissionState.DeniedAlways)
      } catch (e: DeniedException) {
        cont.resume(PermissionState.Denied)
      } catch (e: RequestCanceledException) {
        cont.resume(PermissionState.NotDetermined)
      }
    }
  }
}
