package com.ujizin.camposer.internal.preview

import androidx.compose.ui.geometry.Offset
import com.ujizin.camposer.state.properties.ScaleType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class PreviewGeometryTest {
  private fun compute(
    scaleType: ScaleType,
    isMirrored: Boolean = false,
    density: Float = 1F,
  ) = assertNotNull(
    PreviewGeometry.compute(
      imageWidth = 400,
      imageHeight = 200,
      canvasWidth = 200F,
      canvasHeight = 200F,
      scaleType = scaleType,
      isMirrored = isMirrored,
      density = density,
    ),
  )

  @Test
  fun test_fit_center_letterboxes_vertically() {
    val geometry = compute(ScaleType.FitCenter)

    assertEquals(0.5F, geometry.scale)
    assertEquals(0F, geometry.offsetX)
    assertEquals(50F, geometry.offsetY)
    assertEquals(200F, geometry.drawWidth)
    assertEquals(100F, geometry.drawHeight)
  }

  @Test
  fun test_fill_center_crops_horizontally() {
    val geometry = compute(ScaleType.FillCenter)

    assertEquals(1F, geometry.scale)
    assertEquals(-100F, geometry.offsetX)
    assertEquals(0F, geometry.offsetY)
  }

  @Test
  fun test_start_alignment_has_no_offset() {
    assertEquals(0F, compute(ScaleType.FitStart).offsetY)
    assertEquals(0F, compute(ScaleType.FillStart).offsetX)
  }

  @Test
  fun test_end_alignment_aligns_to_far_edge() {
    assertEquals(100F, compute(ScaleType.FitEnd).offsetY)
    assertEquals(-200F, compute(ScaleType.FillEnd).offsetX)
  }

  @Test
  fun test_to_preview_position_applies_scale_offset_and_density() {
    val geometry = compute(ScaleType.FitCenter, density = 2F)

    val position = geometry.toPreviewPosition(x = 100F, y = 100F)

    assertEquals(Offset(x = 25F, y = 50F), position)
  }

  @Test
  fun test_to_preview_position_mirrors_horizontally() {
    val geometry = compute(ScaleType.FitCenter, isMirrored = true)

    val position = geometry.toPreviewPosition(x = 0F, y = 0F)

    assertEquals(Offset(x = 200F, y = 50F), position)
  }

  @Test
  fun test_compute_returns_null_for_empty_sizes() {
    assertNull(
      PreviewGeometry.compute(
        imageWidth = 0,
        imageHeight = 200,
        canvasWidth = 200F,
        canvasHeight = 200F,
        scaleType = ScaleType.FitCenter,
        isMirrored = false,
        density = 1F,
      ),
    )
    assertNull(
      PreviewGeometry.compute(
        imageWidth = 400,
        imageHeight = 200,
        canvasWidth = 0F,
        canvasHeight = 200F,
        scaleType = ScaleType.FitCenter,
        isMirrored = false,
        density = 1F,
      ),
    )
  }

  @Test
  fun test_equality_compares_all_fields() {
    assertEquals(compute(ScaleType.FitCenter), compute(ScaleType.FitCenter))
    assertEquals(compute(ScaleType.FitCenter).hashCode(), compute(ScaleType.FitCenter).hashCode())
    assert(compute(ScaleType.FitCenter) != compute(ScaleType.FitCenter, isMirrored = true))
  }
}
