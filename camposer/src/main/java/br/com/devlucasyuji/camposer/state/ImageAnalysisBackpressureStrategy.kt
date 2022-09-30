package br.com.devlucasyuji.camposer.state

import androidx.camera.core.ImageAnalysis
import androidx.compose.runtime.Immutable

@Immutable
enum class ImageAnalysisBackpressureStrategy(internal val strategy: Int) {
    KeepOnlyLatest(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST),
    BlockProducer(ImageAnalysis.STRATEGY_BLOCK_PRODUCER);

    companion object {
        fun find(strategy: Int) = values().firstOrNull { it.strategy == strategy } ?: KeepOnlyLatest
    }
}