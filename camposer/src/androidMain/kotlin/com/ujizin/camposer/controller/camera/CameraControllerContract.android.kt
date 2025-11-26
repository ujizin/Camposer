package com.ujizin.camposer.controller.camera

import com.ujizin.camposer.controller.takepicture.AndroidTakePictureCommand
import com.ujizin.camposer.controller.record.AndroidRecordController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import kotlinx.coroutines.flow.StateFlow

internal actual interface CameraControllerContract :
    AndroidRecordController,
    AndroidTakePictureCommand {

    actual val state: CameraState?
    actual val info: CameraInfo?
    actual val isRunning: StateFlow<Boolean>
    actual fun setZoomRatio(zoomRatio: Float)
    actual fun setExposureCompensation(exposureCompensation: Float)
    actual fun setOrientationStrategy(strategy: OrientationStrategy)
    actual fun setFlashMode(flashMode: FlashMode): Result<Unit>
    actual fun setTorchEnabled(isTorchEnabled: Boolean): Result<Unit>
    actual fun setVideoFrameRate(frameRate: Int): Result<Unit>
    actual fun setVideoStabilizationEnabled(mode: VideoStabilizationMode): Result<Unit>
}