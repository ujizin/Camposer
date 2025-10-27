package com.ujizin.camposer.controller.camera

import com.ujizin.camposer.command.TakePictureCommand
import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import kotlinx.coroutines.flow.StateFlow

public actual interface CameraControllerContract : RecordController, TakePictureCommand {
    public actual val state: CameraState?
    public actual val info: CameraInfo?
    public actual val isRunning: StateFlow<Boolean>
    public actual fun setZoomRatio(zoomRatio: Float)
    public actual fun setExposureCompensation(exposureCompensation: Float)
    public actual fun setFlashMode(flashMode: FlashMode)
    public actual fun setTorchEnabled(isTorchEnabled: Boolean)
    public actual fun setOrientationStrategy(strategy: OrientationStrategy)
}