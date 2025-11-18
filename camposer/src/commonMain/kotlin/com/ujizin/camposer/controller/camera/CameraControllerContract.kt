package com.ujizin.camposer.controller.camera

import com.ujizin.camposer.command.TakePictureCommand
import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import kotlinx.coroutines.flow.StateFlow

public expect interface CameraControllerContract : RecordController, TakePictureCommand {
    public val state: CameraState?
    public val info: CameraInfo?
    public val isRunning: StateFlow<Boolean>
    public fun setZoomRatio(zoomRatio: Float)
    public fun setExposureCompensation(exposureCompensation: Float)
    public fun setFlashMode(flashMode: FlashMode)
    public fun setTorchEnabled(isTorchEnabled: Boolean)
    public fun setOrientationStrategy(strategy: OrientationStrategy)
    public fun setVideoFrameRate(frameRate: Int)
    public fun setVideoStabilizationEnabled(isVideoStabilizationEnabled: Boolean)
}
