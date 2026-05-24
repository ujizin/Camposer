package com.ujizin.camposer.state.properties

import androidx.camera.core.ImageAnalysis
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ImageAnalysisBackpressureStrategyAndroidTest {
  @Test
  fun test_keep_only_latest_strategy_value() {
    assertEquals(
      ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST,
      ImageAnalysisBackpressureStrategy.KeepOnlyLatest.strategy,
    )
  }

  @Test
  fun test_block_producer_strategy_value() {
    assertEquals(
      ImageAnalysis.STRATEGY_BLOCK_PRODUCER,
      ImageAnalysisBackpressureStrategy.BlockProducer.strategy,
    )
  }

  @Test
  fun test_find_keep_only_latest_by_strategy_value() {
    assertEquals(
      ImageAnalysisBackpressureStrategy.KeepOnlyLatest,
      ImageAnalysisBackpressureStrategy.find(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST),
    )
  }

  @Test
  fun test_find_block_producer_by_strategy_value() {
    assertEquals(
      ImageAnalysisBackpressureStrategy.BlockProducer,
      ImageAnalysisBackpressureStrategy.find(ImageAnalysis.STRATEGY_BLOCK_PRODUCER),
    )
  }

  @Test
  fun test_find_unknown_strategy_defaults_to_keep_only_latest() {
    assertEquals(
      ImageAnalysisBackpressureStrategy.KeepOnlyLatest,
      ImageAnalysisBackpressureStrategy.find(-1),
    )
  }

  @Test
  fun test_strategy_values_are_distinct() {
    val strategies = ImageAnalysisBackpressureStrategy.entries.map { it.strategy }
    assertEquals(strategies.size, strategies.distinct().size)
  }
}
