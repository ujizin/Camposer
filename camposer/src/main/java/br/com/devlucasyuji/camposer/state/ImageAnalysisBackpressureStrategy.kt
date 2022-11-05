package br.com.devlucasyuji.camposer.state

import androidx.camera.core.ImageAnalysis

/**
 * Image analysis backpressure strategy is used to camera operation mode.
 *
 * https://developer.android.com/training/camerax/analyze#operating-modes
 * */
enum class ImageAnalysisBackpressureStrategy(internal val strategy: Int) {
    /**
     * always caches the latest image, use to non blocking operation.
     * */
    KeepOnlyLatest(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST),

    /**
     *  add multiple images to the internal image queue and begins dropping frames only when the queue is full, use to blocking operation.
     * */
    BlockProducer(ImageAnalysis.STRATEGY_BLOCK_PRODUCER);

    companion object {
        internal fun find(strategy: Int) = values().firstOrNull { it.strategy == strategy } ?: KeepOnlyLatest
    }
}