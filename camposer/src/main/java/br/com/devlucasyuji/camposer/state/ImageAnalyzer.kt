package br.com.devlucasyuji.camposer.state

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.Immutable

/**
 * Intermediate Image analyzer from cameraX
 *
 * @param imageAnalysisBackpressureStrategy the backpressure strategy applied to the image producer
 * @param imageAnalysisTargetSize the intended output size for ImageAnalysis
 * @param imageAnalysisImageQueueDepth the image queue depth of ImageAnalysis.
 * @param analyzerCallback receive images and perform custom processing.
 *
 * @see rememberImageAnalyzer
 * */
@Immutable
class ImageAnalyzer(
    private val cameraState: CameraState,
    imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy,
    imageAnalysisTargetSize: ImageAnalysisTargetSize?,
    imageAnalysisImageQueueDepth: Int,
    private var analyzerCallback: (ImageProxy) -> Unit,
) {

    /**
     * Hold Image analysis Analyzer to camera.
     * */
    internal val analyzer: ImageAnalysis.Analyzer = Analyzer()

    init {
        updateCameraState(
            imageAnalysisBackpressureStrategy,
            imageAnalysisTargetSize,
            imageAnalysisImageQueueDepth
        )
    }

    private fun updateCameraState(
        imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy,
        imageAnalysisTargetSize: ImageAnalysisTargetSize?,
        imageAnalysisImageQueueDepth: Int,
    ) = with(cameraState) {
        this.imageAnalysisBackpressureStrategy = imageAnalysisBackpressureStrategy.strategy
        this.imageAnalysisTargetSize = imageAnalysisTargetSize?.toOutputSize()
        this.imageAnalysisImageQueueDepth = imageAnalysisImageQueueDepth
    }

    @Immutable
    private inner class Analyzer : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            this@ImageAnalyzer.analyzerCallback(image)
        }
    }

    /**
     * Update actual image analysis instance.
     * */
    fun update(
        imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy = ImageAnalysisBackpressureStrategy.find(
            cameraState.imageAnalysisBackpressureStrategy
        ),
        imageAnalysisTargetSize: ImageAnalysisTargetSize? = ImageAnalysisTargetSize(cameraState.imageAnalysisTargetSize),
        imageAnalysisImageQueueDepth: Int = cameraState.imageAnalysisImageQueueDepth,
        analyzerCallback: (ImageProxy) -> Unit = this.analyzerCallback,
    ) {
        updateCameraState(
            imageAnalysisBackpressureStrategy,
            imageAnalysisTargetSize,
            imageAnalysisImageQueueDepth
        )
        this.analyzerCallback = analyzerCallback
    }
}
