package com.ujizin.camposer.state.properties.selector

import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.CameraSelector.LENS_FACING_EXTERNAL
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT
import androidx.camera.core.CameraSelector.LENS_FACING_UNKNOWN
import androidx.camera.core.ExperimentalLensFacing
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalLensFacing::class)
internal class CamPositionAndroidTest {
  @Test
  fun test_back_maps_to_lens_facing_back() {
    assertEquals(LENS_FACING_BACK, CamPosition.Back.value)
  }

  @Test
  fun test_front_maps_to_lens_facing_front() {
    assertEquals(LENS_FACING_FRONT, CamPosition.Front.value)
  }

  @Test
  fun test_external_maps_to_lens_facing_external() {
    assertEquals(LENS_FACING_EXTERNAL, CamPosition.External.value)
  }

  @Test
  fun test_unknown_maps_to_lens_facing_unknown() {
    assertEquals(LENS_FACING_UNKNOWN, CamPosition.Unknown.value)
  }

  @Test
  fun test_find_by_lens_back() {
    assertEquals(CamPosition.Back, CamPosition.findByLens(LENS_FACING_BACK))
  }

  @Test
  fun test_find_by_lens_front() {
    assertEquals(CamPosition.Front, CamPosition.findByLens(LENS_FACING_FRONT))
  }

  @Test
  fun test_find_by_lens_external() {
    assertEquals(CamPosition.External, CamPosition.findByLens(LENS_FACING_EXTERNAL))
  }

  @Test
  fun test_find_by_lens_unknown_int_returns_unknown() {
    assertEquals(CamPosition.Unknown, CamPosition.findByLens(-99))
  }

  @Test
  fun test_value_and_find_by_lens_are_inverse() {
    CamPosition.entries.forEach { position ->
      assertEquals(position, CamPosition.findByLens(position.value))
    }
  }
}
