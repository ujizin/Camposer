package com.ujizin.camposer.fake

import androidx.compose.ui.graphics.ImageBitmap
import com.ujizin.camposer.internal.core.jvm.JvmFrame
import org.bytedeco.javacv.Frame
import java.awt.image.BufferedImage

internal class FakeJvmFrame(
  override val width: Int = 4,
  override val height: Int = 2,
) : JvmFrame {
  var jpegQuality: Int? = null
    private set

  var wasJpegMirrored: Boolean? = null
    private set

  var wasRecorderFrameMirrored: Boolean? = null
    private set

  override fun toImageBitmap(): ImageBitmap = ImageBitmap(width, height)

  override fun toBufferedImage(): BufferedImage =
    BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)

  override fun toRecorderFrame(isMirrored: Boolean): Frame {
    wasRecorderFrameMirrored = isMirrored
    return Frame()
  }

  override fun encodeJpeg(
    quality: Int,
    isMirrored: Boolean,
  ): ByteArray {
    jpegQuality = quality
    wasJpegMirrored = isMirrored
    return byteArrayOf(quality.toByte())
  }
}
