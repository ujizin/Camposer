package com.ujizin.camposer.internal.core.jvm

import androidx.compose.ui.graphics.ImageBitmap
import org.bytedeco.javacv.Frame
import java.awt.image.BufferedImage

/**
 * One frame read from a [JvmVideoSource].
 *
 * The frame is valid until the next read on the same source. Every conversion copies the
 * pixels, so the results outlive the frame.
 */
internal interface JvmFrame {
  val width: Int

  val height: Int

  /** Copies the frame into an [ImageBitmap] for the preview. */
  fun toImageBitmap(): ImageBitmap

  /** Copies the frame into a `TYPE_3BYTE_BGR` [BufferedImage] for analyzers. */
  fun toBufferedImage(): BufferedImage

  /** Converts the frame for the video recorder. */
  fun toRecorderFrame(isMirrored: Boolean): Frame

  /** Encodes the frame as JPEG. */
  fun encodeJpeg(
    quality: Int,
    isMirrored: Boolean,
  ): ByteArray
}
