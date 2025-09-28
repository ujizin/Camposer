package com.ujizin.camposer.state

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.compose.runtime.Stable

/**
 * Intermediate Image analyzer from cameraX
 *
 * @param imageAnalysisBackpressureStrategy the backpressure strategy applied to the image producer
 * @param resolutionSelector the intended resolution for ImageAnalysis
 * @param imageAnalysisImageQueueDepth the image queue depth of ImageAnalysis.
 * @param analyzer receive images and perform custom processing.
 *
 * @see rememberImageAnalyzer
 * */
@Stable
public actual class ImageAnalyzer(
    private val cameraState: CameraState,
    imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy = ImageAnalysisBackpressureStrategy.find(
        cameraState.imageAnalysisBackpressureStrategy
    ),
    resolutionSelector: ResolutionSelector? = null,
    imageAnalysisImageQueueDepth: Int = cameraState.imageAnalysisImageQueueDepth,
    internal var analyzer: ImageAnalysis.Analyzer,
) {

    init {
        updateCameraState(
            imageAnalysisBackpressureStrategy,
            resolutionSelector,
            imageAnalysisImageQueueDepth
        )
    }

    private fun updateCameraState(
        imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy,
        imageAnalysisSelector: ResolutionSelector?,
        imageAnalysisImageQueueDepth: Int,
    ) = with(cameraState) {
        this.imageAnalysisBackpressureStrategy = imageAnalysisBackpressureStrategy.strategy
        this.imageAnalysisResolutionSelector = imageAnalysisSelector
        this.imageAnalysisImageQueueDepth = imageAnalysisImageQueueDepth
    }

    /**
     * Update actual image analysis instance.
     * */
    public fun update(
        imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy = ImageAnalysisBackpressureStrategy.find(
            cameraState.imageAnalysisBackpressureStrategy
        ),
        imageAnalysisTargetSize: ResolutionSelector? = cameraState.imageAnalysisResolutionSelector,
        imageAnalysisImageQueueDepth: Int = cameraState.imageAnalysisImageQueueDepth,
        analyzer: ImageAnalysis.Analyzer = this.analyzer,
    ) {
        updateCameraState(
            imageAnalysisBackpressureStrategy,
            imageAnalysisTargetSize,
            imageAnalysisImageQueueDepth
        )
        this.analyzer = analyzer
    }
}
