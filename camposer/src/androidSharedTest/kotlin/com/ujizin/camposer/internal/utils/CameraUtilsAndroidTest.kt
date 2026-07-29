package com.ujizin.camposer.internal.utils

import android.media.CamcorderProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class CameraUtilsAndroidTest {
  @Test
  fun test_resolution_for_each_camcorder_profile_quality() {
    val expected = mapOf(
      CamcorderProfile.QUALITY_QCIF to 176 * 144,
      CamcorderProfile.QUALITY_QVGA to 320 * 240,
      CamcorderProfile.QUALITY_CIF to 352 * 288,
      CamcorderProfile.QUALITY_VGA to 640 * 480,
      CamcorderProfile.QUALITY_480P to 720 * 480,
      CamcorderProfile.QUALITY_720P to 1280 * 720,
      CamcorderProfile.QUALITY_1080P to 1920 * 1080,
      CamcorderProfile.QUALITY_2K to 2048 * 1080,
      CamcorderProfile.QUALITY_QHD to 2560 * 1440,
      CamcorderProfile.QUALITY_2160P to 3840 * 2160,
      CamcorderProfile.QUALITY_4KDCI to 4096 * 2160,
      CamcorderProfile.QUALITY_8KUHD to 7680 * 4320,
    )

    expected.forEach { (quality, resolution) ->
      assertEquals(resolution, CameraUtils.getResolutionForCamcorderProfileQuality(quality))
    }
  }

  @Test
  fun test_resolution_grows_with_quality() {
    val qualities = listOf(
      CamcorderProfile.QUALITY_QCIF,
      CamcorderProfile.QUALITY_QVGA,
      CamcorderProfile.QUALITY_CIF,
      CamcorderProfile.QUALITY_VGA,
      CamcorderProfile.QUALITY_480P,
      CamcorderProfile.QUALITY_720P,
      CamcorderProfile.QUALITY_1080P,
      CamcorderProfile.QUALITY_2K,
      CamcorderProfile.QUALITY_QHD,
      CamcorderProfile.QUALITY_2160P,
      CamcorderProfile.QUALITY_4KDCI,
      CamcorderProfile.QUALITY_8KUHD,
    ).map(CameraUtils::getResolutionForCamcorderProfileQuality)

    assertEquals(qualities.sorted(), qualities)
  }

  @Test
  fun test_resolution_for_unknown_quality_throws() {
    val error = assertFailsWith<Error> {
      CameraUtils.getResolutionForCamcorderProfileQuality(UNKNOWN_QUALITY)
    }

    assertTrue(error.message.orEmpty().contains(UNKNOWN_QUALITY.toString()))
  }

  private companion object {
    const val UNKNOWN_QUALITY = -1
  }
}
