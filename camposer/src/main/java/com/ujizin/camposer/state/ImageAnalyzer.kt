package com.ujizin.camposer.state

import androidx.camera.core.ImageAnalysis
import androidx.compose.runtime.Immutable

/**
 * Intermediate Image analyzer from cameraX
 *
 * @param imageAnalysisBackpressureStrategy the backpressure strategy applied to the image producer
 * @param imageAnalysisTargetSize the intended output size for ImageAnalysis
 * @param imageAnalysisImageQueueDepth the image queue depth of ImageAnalysis.
 * @param analyzer receive images and perform custom processing.
 *
 * @see rememberImageAnalyzer
 * */
@Immutable
public class ImageAnalyzer(
    private val cameraState: CameraState,
    imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy,
    imageAnalysisTargetSize: ImageTargetSize?,
    imageAnalysisImageQueueDepth: Int,
    internal var analyzer: ImageAnalysis.Analyzer,
) {

    init {
        updateCameraState(
            imageAnalysisBackpressureStrategy,
            imageAnalysisTargetSize,
            imageAnalysisImageQueueDepth
        )
    }

    private fun updateCameraState(
        imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy,
        imageAnalysisTargetSize: ImageTargetSize?,
        imageAnalysisImageQueueDepth: Int,
    ) = with(cameraState) {
        this.imageAnalysisBackpressureStrategy = imageAnalysisBackpressureStrategy.strategy
        this.imageAnalysisTargetSize = imageAnalysisTargetSize?.toOutputSize()
        this.imageAnalysisImageQueueDepth = imageAnalysisImageQueueDepth
    }

    /**
     * Update actual image analysis instance.
     * */
    public fun update(
        imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy = ImageAnalysisBackpressureStrategy.find(
            cameraState.imageAnalysisBackpressureStrategy
        ),
        imageAnalysisTargetSize: ImageTargetSize? = ImageTargetSize(cameraState.imageAnalysisTargetSize),
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
