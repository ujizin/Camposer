package com.ujizin.camposer.controller.camera

import com.ujizin.camposer.controller.takepicture.TakePictureCommand
import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import kotlinx.coroutines.flow.StateFlow

internal expect interface CameraControllerContract : RecordController, TakePictureCommand {
    val state: CameraState?
    val info: CameraInfo?
    val isRunning: StateFlow<Boolean>
    fun setZoomRatio(zoomRatio: Float)
    fun setExposureCompensation(exposureCompensation: Float)
    fun setOrientationStrategy(strategy: OrientationStrategy)
    fun setFlashMode(flashMode: FlashMode) : Result<Unit>
    fun setTorchEnabled(isTorchEnabled: Boolean): Result<Unit>
    fun setVideoFrameRate(frameRate: Int): Result<Unit>
    fun setVideoStabilizationEnabled(mode: VideoStabilizationMode): Result<Unit>
}
