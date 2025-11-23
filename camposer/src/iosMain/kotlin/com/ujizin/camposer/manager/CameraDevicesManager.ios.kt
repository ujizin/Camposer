package com.ujizin.camposer.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ujizin.camposer.state.properties.selector.CamLensType
import com.ujizin.camposer.state.properties.selector.CamPosition
import com.ujizin.camposer.state.properties.selector.CameraId
import com.ujizin.camposer.utils.CameraFormatUtils
import com.ujizin.camposer.utils.DispatchQueue.cameraQueue
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

public actual class CameraDevicesManager {

    private val _cameraDeviceState = MutableStateFlow<CameraDeviceState>(CameraDeviceState.Initial)
    public actual val cameraDevicesState: StateFlow<CameraDeviceState> = _cameraDeviceState
        .asStateFlow()

    private val cameraPresenceMonitor = CameraPresenceMonitor()

    private val cameraPresenceListener = object : CameraPresenceMonitor.Listener {
        override fun onCameraAdded(device: AVCaptureDevice) {
            _cameraDeviceState.updateOnlyIfReady { state ->
                state.copy(devices = state.devices + device.toCameraDevice())
            }
        }

        override fun onCameraRemoved(device: AVCaptureDevice) {
            _cameraDeviceState.updateOnlyIfReady { state ->
                val cameraDeviceToBeDeleted = state.devices.find {
                    it.cameraId.uniqueId == device.uniqueID
                } ?: return@updateOnlyIfReady state

                state.copy(devices = state.devices - cameraDeviceToBeDeleted)
            }
        }
    }

    init {
        dispatch_async(cameraQueue) {
            _cameraDeviceState.update { CameraDeviceState.Devices(getAvailableCameras()) }
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
        )

        val discovery = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
            deviceTypes = deviceTypes,
            mediaType = AVMediaTypeVideo,
            position = AVCaptureDevicePositionUnspecified
        )

        return discovery.devices
            .filterIsInstance<AVCaptureDevice>()
            .map { device -> device.toCameraDevice() }
    }


    private fun AVCaptureDevice.toCameraDevice(): CameraDevice {
        val formats = formats.filterIsInstance<AVCaptureDeviceFormat>()
        return CameraDevice(
            cameraId = CameraId(uniqueID),
            position = getCamPosition(),
            lensType = CamLensType.getPhysicalLensByVirtual(deviceType),
            photoData = CameraFormatUtils.getPhotoFormats(formats),
            videoData = CameraFormatUtils.getVideoFormats(formats),
        )
    }

    private fun AVCaptureDevice.getCamPosition(): CamPosition = when (position) {
        AVCaptureDevicePositionFront -> CamPosition.Front
        AVCaptureDevicePositionBack -> CamPosition.Back
        else -> when (deviceType) {
            AVCaptureDeviceTypeExternal -> CamPosition.External
            else -> CamPosition.Unknown
        }
    }

    private fun MutableStateFlow<CameraDeviceState>.updateOnlyIfReady(
        block: (CameraDeviceState.Devices) -> CameraDeviceState,
    ) {
        if (value is CameraDeviceState.Devices) {
            update { block(it as CameraDeviceState.Devices) }
        }
    }

    public actual fun release() {
        cameraPresenceMonitor.removeCameraPresenceListener(cameraPresenceListener)
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