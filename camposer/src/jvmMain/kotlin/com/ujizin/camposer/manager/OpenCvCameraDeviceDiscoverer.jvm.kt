package com.ujizin.camposer.manager

import com.ujizin.camposer.state.properties.selector.CamLensType
import com.ujizin.camposer.state.properties.selector.CamPosition
import com.ujizin.camposer.state.properties.selector.CameraId
import org.bytedeco.javacv.OpenCVFrameGrabber

internal object OpenCvCameraDeviceDiscoverer : CameraDeviceDiscoverer {
  override fun discoverDevices(): CameraDeviceState {
    val devices = mutableListOf<CameraDevice>()
    for (index in 0..9) {
      val grabber = OpenCVFrameGrabber(index)
      try {
        grabber.start()
        devices += CameraDevice(
          cameraId = CameraId(index.toString()),
          name = "Camera $index",
          position = CamPosition.External,
          fov = 0f,
          lensType = listOf(CamLensType.Wide),
          photoData = emptyList(),
          videoData = emptyList(),
        )
        grabber.stop()
      } catch (_: Exception) {
        break
      }
    }
    return if (devices.isEmpty()) CameraDeviceState.Initial else CameraDeviceState.Devices(devices)
  }
}

