package com.ujizin.camposer.session

import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.ImplementationMode
import com.ujizin.camposer.state.properties.MirrorMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.format.Default
import com.ujizin.camposer.state.properties.selector.CamSelector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class CameraStateTest : CameraSessionTest() {
  // region default values

  @Test
  fun test_default_capture_mode_is_image() {
    assertEquals(CaptureMode.Image, cameraSession.state.captureMode.value)
  }

  @Test
  fun test_default_cam_selector_is_back() {
    assertEquals(CamSelector.Back, cameraSession.state.camSelector.value)
  }

  @Test
  fun test_default_scale_type_is_fill_center() {
    assertEquals(ScaleType.FillCenter, cameraSession.state.scaleType.value)
  }

  @Test
  fun test_default_flash_mode_is_off() {
    assertEquals(FlashMode.Off, cameraSession.state.flashMode.value)
  }

  @Test
  fun test_default_mirror_mode_is_only_in_front() {
    assertEquals(MirrorMode.OnlyInFront, cameraSession.state.mirrorMode.value)
  }

  @Test
  fun test_default_cam_format_is_default() {
    assertEquals(CamFormat.Default, cameraSession.state.camFormat.value)
  }

  @Test
  fun test_default_implementation_mode_is_performance() {
    assertEquals(ImplementationMode.Performance, cameraSession.state.implementationMode.value)
  }

  @Test
  fun test_default_image_analyzer_is_null() {
    assertNull(cameraSession.state.imageAnalyzer.value)
  }

  @Test
  fun test_default_image_analyzer_enabled_is_false() {
    assertFalse(cameraSession.state.isImageAnalyzerEnabled.value)
  }

  @Test
  fun test_default_pinch_to_zoom_is_enabled() {
    assertTrue(cameraSession.state.isPinchToZoomEnabled.value)
  }

  @Test
  fun test_default_exposure_compensation_is_zero() {
    assertEquals(0F, cameraSession.state.exposureCompensation.value)
  }

  @Test
  fun test_default_image_capture_strategy_is_balanced() {
    assertEquals(ImageCaptureStrategy.Balanced, cameraSession.state.imageCaptureStrategy.value)
  }

  @Test
  fun test_default_focus_on_tap_is_enabled() {
    assertTrue(cameraSession.state.isFocusOnTapEnabled.value)
  }

  @Test
  fun test_default_torch_is_disabled() {
    assertFalse(cameraSession.state.isTorchEnabled.value)
  }

  @Test
  fun test_default_orientation_strategy_is_device() {
    assertEquals(OrientationStrategy.Device, cameraSession.state.orientationStrategy.value)
  }

  @Test
  fun test_default_frame_rate_is_negative_one() {
    assertEquals(-1, cameraSession.state.frameRate.value)
  }

  @Test
  fun test_default_video_stabilization_mode_is_off() {
    assertEquals(VideoStabilizationMode.Off, cameraSession.state.videoStabilizationMode.value)
  }

  // endregion

  // region clamping

  @Test
  fun test_update_exposure_compensation_clamped_to_max() {
    initCameraSession()
    val maxExposure = cameraSession.info.state.value.maxExposure

    cameraSession.state.updateExposureCompensation(Float.MAX_VALUE)

    assertEquals(maxExposure, cameraSession.state.exposureCompensation.value)
  }

  @Test
  fun test_update_exposure_compensation_clamped_to_min() {
    initCameraSession()
    val minExposure = cameraSession.info.state.value.minExposure

    cameraSession.state.updateExposureCompensation(-Float.MAX_VALUE)

    assertEquals(minExposure, cameraSession.state.exposureCompensation.value)
  }

  @Test
  fun test_update_zoom_ratio_clamped_to_max() {
    initCameraSession()
    val maxZoom = cameraSession.info.state.value.maxZoom

    cameraSession.state.updateZoomRatio(Float.MAX_VALUE)

    assertEquals(maxZoom, cameraSession.state.zoomRatio.value)
  }

  @Test
  fun test_update_zoom_ratio_clamped_to_min() {
    initCameraSession()
    val minZoom = cameraSession.info.state.value.minZoom

    cameraSession.state.updateZoomRatio(-Float.MAX_VALUE)

    assertEquals(minZoom, cameraSession.state.zoomRatio.value)
  }

  // endregion
}
