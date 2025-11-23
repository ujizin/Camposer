package com.ujizin.camposer.manager

/**
 * Camera Device State for [CameraDevicesManager.cameraDevicesState]
 *
 * @see CameraDevicesManager
 * */
public sealed interface CameraDeviceState {

    /**
     * Represents the initial state of the camera device.
     *
     * This state indicates that the camera device discovery or initialization process
     * has not yet completed.
     */
    public data object Initial : CameraDeviceState

    /**
     * Represents the state where camera devices have been successfully loaded.
     *
     * @property devices The list of available [CameraDevice]s.
     */
    public data class Devices(val devices: List<CameraDevice>) : CameraDeviceState
}
