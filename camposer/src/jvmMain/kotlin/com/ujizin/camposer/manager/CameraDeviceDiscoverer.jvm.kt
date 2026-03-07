package com.ujizin.camposer.manager

internal fun interface CameraDeviceDiscoverer {
  fun discoverDevices(): CameraDeviceState
}

