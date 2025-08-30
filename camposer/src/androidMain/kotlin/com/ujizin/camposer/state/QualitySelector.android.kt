package com.ujizin.camposer.state

import androidx.camera.video.Quality as CameraXQuality
import androidx.camera.video.QualitySelector as CameraXQualitySelector

public actual class QualitySelector(
    internal val qualitySelector: CameraXQualitySelector
) {

    public actual companion object {
        public actual fun from(
            quality: Quality
        ): QualitySelector = QualitySelector(
            CameraXQualitySelector.from(quality.quality)
        )
    }
}

public actual class Quality(internal val quality: CameraXQuality)