package com.ujizin.camposer.state

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.view.CameraController
import androidx.compose.runtime.Stable
import com.ujizin.camposer.config.config

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
    private val controller: CameraController,
    imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy = ImageAnalysisBackpressureStrategy.find(
        controller.imageAnalysisBackpressureStrategy
    ),
    resolutionSelector: ResolutionSelector? = null,
    imageAnalysisImageQueueDepth: Int = controller.imageAnalysisImageQueueDepth,
    internal var analyzer: ImageAnalysis.Analyzer,
) {

    /**
     * Image analysis backpressure strategy, use [rememberImageAnalyzer] to set value.
     * */
    internal var imageAnalysisBackpressureStrategy: Int
        get() = controller.imageAnalysisBackpressureStrategy
        set(value) {
            if (imageAnalysisBackpressureStrategy != value) {
                controller.imageAnalysisBackpressureStrategy = value
            }
        }

    /**
     * Image analysis target size, use [rememberImageAnalyzer] to set value.
     * @see rememberImageAnalyzer
     * */
    internal var imageAnalysisResolutionSelector: ResolutionSelector?
        get() = controller.imageAnalysisResolutionSelector
        set(value) {
            // TODO check if this works as expected
            if (value != null && imageAnalysisResolutionSelector != value) {
                controller.imageAnalysisResolutionSelector = value
            }
        }

    /**
     * Image analysis image queue depth, use [rememberImageAnalyzer] to set value.
     * @see rememberImageAnalyzer
     * */
    public var imageAnalysisImageQueueDepth: Int by config(controller.imageAnalysisImageQueueDepth) {
        controller.imageAnalysisImageQueueDepth = it
    }
        internal set

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
    ) {
        this.imageAnalysisBackpressureStrategy = imageAnalysisBackpressureStrategy.strategy
        this.imageAnalysisResolutionSelector = imageAnalysisSelector
        this.imageAnalysisImageQueueDepth = imageAnalysisImageQueueDepth
    }

    /**
     * Update actual image analysis instance.
     * */
    public fun update(
        imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy = ImageAnalysisBackpressureStrategy.find(
            controller.imageAnalysisBackpressureStrategy
        ),
        imageAnalysisTargetSize: ResolutionSelector? = controller.imageAnalysisResolutionSelector,
        imageAnalysisImageQueueDepth: Int = controller.imageAnalysisImageQueueDepth,
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
