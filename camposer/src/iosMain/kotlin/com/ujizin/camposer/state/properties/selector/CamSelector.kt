package com.ujizin.camposer.state.properties.selector

import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront

public actual class CamSelector {

    internal val position: AVCaptureDevicePosition

    public actual val isFront: Boolean
        get() = position == AVCaptureDevicePositionFront

    public constructor(camPosition: CamPosition) : this(
        camPosition = camPosition,
        position = when (camPosition) {
            CamPosition.Back -> AVCaptureDevicePositionBack
            CamPosition.Front -> AVCaptureDevicePositionFront
        }
    )

    internal constructor(
        camPosition: CamPosition,
        position: AVCaptureDevicePosition,
    ) {
        this.position = position
    }

    public actual companion object {
        public actual val Front: CamSelector = CamSelector(CamPosition.Front)
        public actual val Back: CamSelector = CamSelector(CamPosition.Back)
    }
}