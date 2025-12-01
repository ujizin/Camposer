package com.ujizin.camposer.manager

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.CameraIdentifier
import androidx.camera.core.CameraPresenceListener
import androidx.camera.core.impl.utils.futures.FutureCallback
import androidx.camera.core.impl.utils.futures.Futures
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.common.util.concurrent.ListenableFuture
import com.ujizin.camposer.extensions.compatMainExecutor
import com.ujizin.camposer.internal.utils.CameraUtils
import com.ujizin.camposer.state.properties.selector.CamPosition
import com.ujizin.camposer.state.properties.selector.CameraId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@SuppressLint("RestrictedApi")
public actual class CameraDevicesManager(
  context: Context,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
  private val mainExecutor = context.compatMainExecutor
  private val mainScope = MainScope()

  private val _cameraDevicesState = MutableStateFlow<CameraDeviceState>(CameraDeviceState.Initial)
  public actual val cameraDevicesState: StateFlow<CameraDeviceState> =
    _cameraDevicesState.asStateFlow()

  private lateinit var cameraProvider: ProcessCameraProvider

  private val cameraPresenceListener =
    object : CameraPresenceListener {
      override fun onCamerasAdded(cameraIdentifiers: Set<CameraIdentifier?>) {
        updateCameraDevices()
      }

      override fun onCamerasRemoved(cameraIdentifiers: Set<CameraIdentifier?>) {
        updateCameraDevices()
      }
    }

  init {
    mainScope.launch {
      cameraProvider = ProcessCameraProvider.getInstance(context).await(mainExecutor)
      updateCameraDevices()
      cameraProvider.addCameraPresenceListener(
        executor = mainExecutor,
        listener = cameraPresenceListener,
      )
    }
  }

  private suspend fun getAvailableCameraDevices(): List<CameraDevice> =
    withContext(dispatcher) {
      cameraProvider.availableCameraInfos.map { info ->
        CameraDevice(
          cameraId =
            CameraId(
              identifier = info.cameraIdentifier,
              physicalCameraInfos = info.physicalCameraInfos,
            ),
          photoData = CameraUtils.getPhotoResolutions(info),
          videoData = CameraUtils.getVideoResolutions(info),
          lensType = CameraUtils.getCamLensTypes(info),
          position = CamPosition.findByLens(info.lensFacing),
        )
      }
    }

  private fun updateCameraDevices() {
    mainScope.launch {
      _cameraDevicesState.update { CameraDeviceState.Devices(getAvailableCameraDevices()) }
    }
  }

  public actual fun release() {
    cameraProvider.removeCameraPresenceListener(cameraPresenceListener)
  }
}

@Composable
public actual fun rememberCameraDeviceState(): State<CameraDeviceState> {
  val context = LocalContext.current
  val cameraManager = remember(context) { CameraDevicesManager(context) }
  val devicesState = cameraManager.cameraDevicesState.collectAsStateWithLifecycle()

  DisposableEffect(context) { onDispose { cameraManager.release() } }

  return devicesState
}

@SuppressLint("RestrictedApi")
private suspend fun <T> ListenableFuture<T>.await(executor: Executor): T =
  suspendCancellableCoroutine { continuation ->
    Futures.addCallback(
      this,
      object : FutureCallback<T> {
        override fun onSuccess(result: T?) {
          if (result == null) {
            continuation.resumeWithException(IllegalStateException("Result not found"))
            return
          }
          continuation.resume(result)
        }

        override fun onFailure(throwable: Throwable) {
          continuation.resumeWithException(throwable)
        }
      },
      executor,
    )
  }
