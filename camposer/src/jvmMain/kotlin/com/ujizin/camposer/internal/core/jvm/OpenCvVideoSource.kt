package com.ujizin.camposer.internal.core.jvm

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import com.ujizin.camposer.internal.error.ErrorEncodePictureException
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacpp.IntPointer
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.opencv.global.opencv_core.flip
import org.bytedeco.opencv.global.opencv_imgcodecs.IMWRITE_JPEG_QUALITY
import org.bytedeco.opencv.global.opencv_imgcodecs.imencode
import org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2BGRA
import org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGRA2BGR
import org.bytedeco.opencv.global.opencv_imgproc.COLOR_GRAY2BGR
import org.bytedeco.opencv.global.opencv_imgproc.COLOR_GRAY2BGRA
import org.bytedeco.opencv.global.opencv_imgproc.cvtColor
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_AUTO_EXPOSURE
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_EXPOSURE
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FPS
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_HEIGHT
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_WIDTH
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_videoio.VideoCapture
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte

private const val BGR_CHANNELS = 3
private const val BGRA_CHANNELS = 4
private const val FLIP_HORIZONTAL = 1

/**
 * [JvmVideoSource] backed by OpenCV's `VideoCapture`.
 */
internal class OpenCvVideoSource : JvmVideoSource {
  private val capture = VideoCapture()
  private val mat = Mat()
  private val frame = MatFrame(mat)

  override val isOpen: Boolean
    get() = capture.isOpened

  override val backendName: String
    get() = if (isOpen) capture.backendName.string else ""

  override fun open(deviceIndex: Int): Boolean = capture.open(deviceIndex)

  override fun read(): JvmFrame? {
    if (!capture.read(mat) || mat.empty()) return null
    return frame
  }

  override fun get(property: JvmVideoProperty): Double = capture.get(property.id)

  override fun set(
    property: JvmVideoProperty,
    value: Double,
  ): Boolean = capture.set(property.id, value)

  override fun release() {
    if (capture.isOpened) capture.release()
    frame.close()
    mat.release()
  }

  internal companion object Factory : JvmVideoSource.Factory {
    override fun create(): JvmVideoSource = OpenCvVideoSource()
  }
}

private val JvmVideoProperty.id: Int
  get() = when (this) {
    JvmVideoProperty.FrameWidth -> CAP_PROP_FRAME_WIDTH
    JvmVideoProperty.FrameHeight -> CAP_PROP_FRAME_HEIGHT
    JvmVideoProperty.FrameRate -> CAP_PROP_FPS
    JvmVideoProperty.Exposure -> CAP_PROP_EXPOSURE
    JvmVideoProperty.AutoExposure -> CAP_PROP_AUTO_EXPOSURE
  }

/**
 * [JvmFrame] over a reused OpenCV [Mat]. Conversions copy the pixels out of the Mat.
 */
internal class MatFrame(
  private val mat: Mat,
) : JvmFrame,
  AutoCloseable {
  private val bgra = Mat()
  private val bgr = Mat()
  private val mirrored = Mat()
  private val frameConverter = OpenCVFrameConverter.ToMat()

  override val width: Int
    get() = mat.cols()

  override val height: Int
    get() = mat.rows()

  override fun toImageBitmap(): ImageBitmap {
    val source = when (mat.channels()) {
      BGRA_CHANNELS -> mat
      BGR_CHANNELS -> mat.toColor(bgra, COLOR_BGR2BGRA)
      else -> mat.toColor(bgra, COLOR_GRAY2BGRA)
    }
    val rowBytes = source.cols() * BGRA_CHANNELS
    val pixels = ByteArray(rowBytes * source.rows())
    source.data().get(pixels)

    val info = ImageInfo(source.cols(), source.rows(), ColorType.BGRA_8888, ColorAlphaType.OPAQUE)
    return Bitmap()
      .apply {
        installPixels(info, pixels, rowBytes)
        setImmutable()
      }.asComposeImageBitmap()
  }

  override fun toBufferedImage(): BufferedImage {
    val source = toBgr()
    val image = BufferedImage(source.cols(), source.rows(), BufferedImage.TYPE_3BYTE_BGR)
    val target = (image.raster.dataBuffer as DataBufferByte).data
    source.data().get(target)
    return image
  }

  override fun toRecorderFrame(isMirrored: Boolean): Frame =
    frameConverter.convert(toBgr().mirroredIf(isMirrored))

  override fun encodeJpeg(
    quality: Int,
    isMirrored: Boolean,
  ): ByteArray {
    val source = toBgr().mirroredIf(isMirrored)
    val buffer = BytePointer()
    val params = IntPointer(IMWRITE_JPEG_QUALITY, quality)
    try {
      if (!imencode(".jpg", source, buffer, params)) throw ErrorEncodePictureException()
      return ByteArray(buffer.limit().toInt()).also { bytes -> buffer.get(bytes) }
    } finally {
      params.close()
      buffer.close()
    }
  }

  override fun close() {
    bgra.release()
    bgr.release()
    mirrored.release()
    frameConverter.close()
  }

  private fun toBgr(): Mat =
    when (mat.channels()) {
      BGR_CHANNELS -> mat
      BGRA_CHANNELS -> mat.toColor(bgr, COLOR_BGRA2BGR)
      else -> mat.toColor(bgr, COLOR_GRAY2BGR)
    }

  private fun Mat.mirroredIf(isMirrored: Boolean): Mat {
    if (!isMirrored) return this
    flip(this, mirrored, FLIP_HORIZONTAL)
    return mirrored
  }

  private fun Mat.toColor(
    target: Mat,
    code: Int,
  ): Mat {
    cvtColor(this, target, code)
    return target
  }
}
