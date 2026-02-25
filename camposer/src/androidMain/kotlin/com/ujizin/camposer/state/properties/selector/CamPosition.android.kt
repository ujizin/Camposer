package com.ujizin.camposer.state.properties.selector

import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.CameraSelector.LENS_FACING_EXTERNAL
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT
import androidx.camera.core.CameraSelector.LENS_FACING_UNKNOWN
import androidx.camera.core.ExperimentalLensFacing

internal val CamPosition.value: Int
  @OptIn(ExperimentalLensFacing::class)
  get() = when (this) {
    CamPosition.Back -> LENS_FACING_BACK
    CamPosition.Front -> LENS_FACING_FRONT
    CamPosition.External -> LENS_FACING_EXTERNAL
    CamPosition.Unknown -> LENS_FACING_UNKNOWN
  }

internal fun CamPosition.Companion.findByLens(lensFacing: Int): CamPosition =
  CamPosition.entries.firstOrNull {
    lensFacing == it.value
  } ?: CamPosition.Unknown
