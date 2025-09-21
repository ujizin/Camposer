package com.ujizin.camposer.state

import android.util.Size
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector as CameraXQualitySelector

public actual enum class ResolutionPreset(
    private val imageQuality: Size? = null,
    private val videoQuality: Quality? = null,
) {
    Default,
    UltraHigh(Size(3840, 2160)),
    High(Size(1920, 1080)),
    Medium(Size(1080, 720)),
    Low(Size(640, 480));

    internal fun getResolutionSelector(): ResolutionSelector? {
        if (imageQuality == null) return null

        return ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    /*boundSize = */ imageQuality,
                    /*fallbackRule = */ ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
                )
            )
            .build()
    }

    internal fun getQualitySelector(): CameraXQualitySelector? {
        if (videoQuality == null) return null

        return CameraXQualitySelector.from(
            videoQuality,
            FallbackStrategy.higherQualityOrLowerThan(videoQuality),
        )
    }
}
