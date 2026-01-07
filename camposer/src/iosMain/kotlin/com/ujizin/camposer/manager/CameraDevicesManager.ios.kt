package com.ujizin.camposer.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ujizin.camposer.internal.utils.CameraFormatUtils
import com.ujizin.camposer.internal.utils.DispatchQueue.cameraQueue
import com.ujizin.camposer.state.properties.selector.CamLensType
import com.ujizin.camposer.state.properties.selector.CamPosition
import com.ujizin.camposer.state.properties.selector.CameraId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDevicePositionUnspecified
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTelephotoCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTripleCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInUltraWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureDeviceTypeExternal
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.deviceType
import platform.AVFoundation.position
import platform.darwin.dispatch_async

internal actual class CameraDevicesManager {
  private val _cameraDevicesState = MutableStateFlow<CameraDeviceState>(CameraDeviceState.Initial)
  actual val cameraDevicesState: StateFlow<CameraDeviceState> = _cameraDevicesState
    .asStateFlow()

  private val cameraPresenceMonitor = CameraPresenceMonitor()

  private val cameraPresenceListener = object : CameraPresenceMonitor.Listener {
    override fun onCameraUpdated() {
      _cameraDevicesState.update { CameraDeviceState.Devices(getAvailableCameras()) }
    }
  }

  init {
    dispatch_async(cameraQueue) {
      _cameraDevicesState.update { CameraDeviceState.Devices(getAvailableCameras()) }
      cameraPresenceMonitor.addCameraPresenceListener(cameraPresenceListener)
    }
  }

  internal fun getAvailableCameras(): List<CameraDevice> {
    val deviceTypes = listOf(
      AVCaptureDeviceTypeBuiltInWideAngleCamera,
      AVCaptureDeviceTypeBuiltInUltraWideCamera,
      AVCaptureDeviceTypeBuiltInTelephotoCamera,
      AVCaptureDeviceTypeBuiltInDualCamera,
      AVCaptureDeviceTypeBuiltInDualWideCamera,
      AVCaptureDeviceTypeBuiltInTripleCamera,
      AVCaptureDeviceTypeExternal,
    )

    val discovery = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
      deviceTypes = deviceTypes,
      mediaType = AVMediaTypeVideo,
      position = AVCaptureDevicePositionUnspecified,
    )

    return discovery.devices
      .filterIsInstance<AVCaptureDevice>()
      .map { device -> device.toCameraDevice() }
  }

  private fun AVCaptureDevice.toCameraDevice(): CameraDevice {
    val formats = formats.filterIsInstance<AVCaptureDeviceFormat>()
    return CameraDevice(
      cameraId = CameraId(uniqueID),
      name = localizedName,
      position = getCamPosition(),
      fov = activeFormat.videoFieldOfView,
      lensType = CamLensType.getPhysicalLensByVirtual(deviceType),
      photoData = CameraFormatUtils.getPhotoFormats(formats),
      videoData = CameraFormatUtils.getVideoFormats(formats),
    )
  }

  private fun AVCaptureDevice.getCamPosition(): CamPosition =
    when (position) {
      AVCaptureDevicePositionFront -> CamPosition.Front

      AVCaptureDevicePositionBack -> CamPosition.Back

      else -> when (deviceType) {
        AVCaptureDeviceTypeExternal -> CamPosition.External
        else -> CamPosition.Unknown
      }
    }

  public actual fun release() {
    cameraPresenceMonitor.removeCameraPresenceListener(cameraPresenceListener)
    cameraPresenceMonitor.release()
  }
}

@Composable
public actual fun rememberCameraDeviceState(): State<CameraDeviceState> {
  val cameraDevicesManager = remember { CameraDevicesManager() }
  val cameraDeviceState = cameraDevicesManager.cameraDevicesState.collectAsStateWithLifecycle()

  DisposableEffect(Unit) {
    onDispose {
      cameraDevicesManager.release()
    }
  }

  return cameraDeviceState
}
