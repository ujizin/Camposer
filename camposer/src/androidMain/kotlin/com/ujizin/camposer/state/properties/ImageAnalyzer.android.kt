package com.ujizin.camposer.state.properties

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.view.CameraController
import androidx.compose.runtime.Stable
import com.ujizin.camposer.state.properties.ImageAnalysisBackpressureStrategy.Companion.find

/**
 * Intermediate Image analyzer from cameraX
 *
 * @param imageAnalysisBackpressureStrategy the backpressure strategy applied to the image producer
 * @param resolutionSelector the intended resolution for ImageAnalysis
 * @param imageAnalysisImageQueueDepth the image queue depth of ImageAnalysis.
 * @param analyzer receive images and perform custom processing.
 *
 * @see com.ujizin.camposer.session.rememberImageAnalyzer
 * */
@Stable
public actual class ImageAnalyzer(
  private val controller: CameraController,
  imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy =
    find(controller.imageAnalysisBackpressureStrategy),
  resolutionSelector: ResolutionSelector? = null,
  imageAnalysisImageQueueDepth: Int = controller.imageAnalysisImageQueueDepth,
  internal var analyzer: ImageAnalysis.Analyzer,
) {
  /**
   * Image analysis backpressure strategy, use [com.ujizin.camposer.session.rememberImageAnalyzer] to set value.
   * */
  internal var imageAnalysisBackpressureStrategy: Int
    get() = controller.imageAnalysisBackpressureStrategy
    set(value) {
      if (imageAnalysisBackpressureStrategy != value) {
        controller.imageAnalysisBackpressureStrategy = value
      }
    }

  /**
   * Image analysis target size, use [com.ujizin.camposer.session.rememberImageAnalyzer] to set value.
   * @see com.ujizin.camposer.session.rememberImageAnalyzer
   * */
  internal var imageAnalysisResolutionSelector: ResolutionSelector?
    get() = controller.imageAnalysisResolutionSelector
    set(value) {
      if (value != null && imageAnalysisResolutionSelector != value) {
        controller.imageAnalysisResolutionSelector = value
      }
    }

  /**
   * Image analysis image queue depth, use [com.ujizin.camposer.session.rememberImageAnalyzer] to set value.
   * @see com.ujizin.camposer.session.rememberImageAnalyzer
   * */
  internal var imageAnalysisImageQueueDepth: Int
    get() = controller.imageAnalysisImageQueueDepth
    set(value) {
      controller.imageAnalysisImageQueueDepth = value
    }

  init {
    updateCamera(
      imageAnalysisBackpressureStrategy,
      resolutionSelector,
      imageAnalysisImageQueueDepth,
    )
  }

  private fun updateCamera(
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
    imageAnalysisBackpressureStrategy: ImageAnalysisBackpressureStrategy =
      find(
        controller.imageAnalysisBackpressureStrategy,
      ),
    imageAnalysisTargetSize: ResolutionSelector? = controller.imageAnalysisResolutionSelector,
    imageAnalysisImageQueueDepth: Int = controller.imageAnalysisImageQueueDepth,
    analyzer: ImageAnalysis.Analyzer = this.analyzer,
  ) {
    updateCamera(
      imageAnalysisBackpressureStrategy,
      imageAnalysisTargetSize,
      imageAnalysisImageQueueDepth,
    )
    this.analyzer = analyzer
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ImageAnalyzer) return false

    if (imageAnalysisBackpressureStrategy !=
      other.imageAnalysisBackpressureStrategy
    ) {
      return false
    }
    if (imageAnalysisResolutionSelector != other.imageAnalysisResolutionSelector) return false
    if (imageAnalysisImageQueueDepth != other.imageAnalysisImageQueueDepth) return false
    if (analyzer != other.analyzer) return false

    return true
  }

  override fun hashCode(): Int {
    var result = 31 * imageAnalysisBackpressureStrategy
    result = 31 * result + (imageAnalysisResolutionSelector?.hashCode() ?: 0)
    result = 31 * result + imageAnalysisImageQueueDepth
    result = 31 * result + analyzer.hashCode()
    return result
  }
}
