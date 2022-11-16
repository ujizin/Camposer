package com.ujizin.camposer.state

import androidx.camera.core.ImageAnalysis

/**
 * Image analysis backpressure strategy is used to camera operation mode.
 *
 * For more information, check it out [CameraX ImageAnalysis operating modes](https://developer.android.com/training/camerax/analyze#operating-modes)
 * */
public enum class ImageAnalysisBackpressureStrategy(internal val strategy: Int) {
    /**
     * always caches the latest image, used for non-blocking operation.
     * */
    KeepOnlyLatest(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST),

    /**
     *  add multiple images to the internal image queue and begin dropping frames only when the queue is full, used for blocking operation.
     * */
    BlockProducer(ImageAnalysis.STRATEGY_BLOCK_PRODUCER);

    internal companion object {
        internal fun find(strategy: Int) =
            values().firstOrNull { it.strategy == strategy } ?: KeepOnlyLatest
    }
}