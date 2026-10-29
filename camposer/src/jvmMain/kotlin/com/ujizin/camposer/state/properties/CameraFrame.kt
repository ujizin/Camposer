package com.ujizin.camposer.state.properties

import androidx.compose.ui.geometry.Offset
import com.ujizin.camposer.internal.preview.PreviewGeometry
import java.awt.image.BufferedImage

/**
 * A camera frame delivered to an [ImageAnalyzer] on desktop.
 *
 * @property image the frame pixels as a `TYPE_3BYTE_BGR` [BufferedImage].
 */
public class CameraFrame internal constructor(
  public val image: BufferedImage,
  private val previewGeometry: PreviewGeometry?,
) {
  /** Creates a frame that is not tied to a preview, for example to test an analyzer. */
  public constructor(image: BufferedImage) : this(image, previewGeometry = null)

  public val width: Int
    get() = image.width

  public val height: Int
    get() = image.height

  /**
   * Maps a pixel of [image] to the preview coordinate space, in dp.
   *
   * Returns [Offset.Unspecified] when the preview has not been laid out yet.
   */
  public fun toPreviewPosition(
    x: Float,
    y: Float,
  ): Offset = previewGeometry?.toPreviewPosition(x, y) ?: Offset.Unspecified
}
