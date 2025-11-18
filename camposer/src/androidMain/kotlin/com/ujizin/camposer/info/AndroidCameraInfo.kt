package com.ujizin.camposer.info

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.view.CameraController
import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.utils.CameraUtils

internal class AndroidCameraInfo(
    private val controller: CameraController,
) {

    internal val initialExposure: Float = INITIAL_EXPOSURE_VALUE
    internal val initialZoom: Float = INITIAL_ZOOM_VALUE

    internal val minZoom: Float
        get() = controller.zoomState.value?.minZoomRatio ?: initialZoom

    internal val maxZoom: Float
        get() = controller.zoomState.value?.maxZoomRatio ?: initialZoom

    private val exposureCompensationRange
        get() = controller.cameraInfo?.exposureState?.exposureCompensationRange

    internal val minExposure: Float
        get() = exposureCompensationRange?.lower?.toFloat() ?: INITIAL_EXPOSURE_VALUE

    internal val maxExposure: Float
        get() = exposureCompensationRange?.upper?.toFloat() ?: INITIAL_EXPOSURE_VALUE

    internal val isFlashSupported: Boolean
        get() = controller.cameraInfo?.hasFlashUnit() ?: false

    internal val isZeroShutterLagSupported: Boolean
        @OptIn(ExperimentalZeroShutterLag::class)
        get() = controller.cameraInfo?.isZslSupported ?: false

    internal val isFocusSupported: Boolean
        get() = controller.cameraInfo?.isFocusMeteringSupported(CameraUtils.createFocusMetering()) ?: false

    internal val photoFormats: List<CameraData>
        get() = controller.cameraInfo?.let(CameraUtils::getPhotoResolutions).orEmpty()

    internal val videoFormats: List<CameraData>
        get() = controller.cameraInfo?.let(CameraUtils::getVideoResolutions).orEmpty()

    companion object {
        private const val INITIAL_ZOOM_VALUE = 1F
        private const val INITIAL_EXPOSURE_VALUE = 0F
    }
}