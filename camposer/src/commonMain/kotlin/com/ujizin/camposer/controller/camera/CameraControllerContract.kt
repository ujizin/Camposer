package com.ujizin.camposer.controller.camera

import com.ujizin.camposer.command.TakePictureCommand
import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import kotlinx.coroutines.flow.StateFlow

public expect interface CameraControllerContract : RecordController, TakePictureCommand {
    public val state: CameraState?
    public val info: CameraInfo?
    public val isRunning: StateFlow<Boolean>
    public fun setZoomRatio(zoomRatio: Float)
    public fun setExposureCompensation(exposureCompensation: Float)
    public fun setOrientationStrategy(strategy: OrientationStrategy)
    public fun setFlashMode(flashMode: FlashMode) : Result<Unit>
    public fun setTorchEnabled(isTorchEnabled: Boolean): Result<Unit>
    public fun setVideoFrameRate(frameRate: Int): Result<Unit>
    public fun setVideoStabilizationEnabled(mode: VideoStabilizationMode): Result<Unit>
}
