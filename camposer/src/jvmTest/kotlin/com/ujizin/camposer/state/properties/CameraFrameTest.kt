package com.ujizin.camposer.state.properties

import androidx.compose.ui.geometry.Offset
import com.ujizin.camposer.internal.preview.PreviewGeometry
import java.awt.image.BufferedImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class CameraFrameTest {
  private val image = BufferedImage(4, 2, BufferedImage.TYPE_3BYTE_BGR)

  @Test
  fun test_size_comes_from_image() {
    val frame = CameraFrame(image, previewGeometry = null)

    assertEquals(4, frame.width)
    assertEquals(2, frame.height)
  }

  @Test
  fun test_position_is_unspecified_without_geometry() {
    val frame = CameraFrame(image, previewGeometry = null)

    assertEquals(Offset.Unspecified, frame.toPreviewPosition(1F, 1F))
  }

  @Test
  fun test_position_is_mapped_with_geometry() {
    val geometry = assertNotNull(
      PreviewGeometry.compute(4, 2, 8F, 4F, ScaleType.FitCenter, false, 1F),
    )
    val frame = CameraFrame(image, geometry)

    assertEquals(Offset(2F, 2F), frame.toPreviewPosition(1F, 1F))
  }
}
