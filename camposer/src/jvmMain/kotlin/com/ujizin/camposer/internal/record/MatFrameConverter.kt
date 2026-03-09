package com.ujizin.camposer.internal.record

import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.opencv.opencv_core.Mat

internal interface MatFrameConverter {
  fun convert(mat: Mat): Frame?

  fun close()
}

internal fun openCVMatFrameConverter(): MatFrameConverter = OpenCVMatFrameConverter()

private class OpenCVMatFrameConverter : MatFrameConverter {
  private val delegate = OpenCVFrameConverter.ToMat()

  override fun convert(mat: Mat): Frame? = delegate.convert(mat)

  override fun close() = delegate.close()
}
