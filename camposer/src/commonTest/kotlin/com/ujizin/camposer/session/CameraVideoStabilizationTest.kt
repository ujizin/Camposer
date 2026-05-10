package com.ujizin.camposer.session

import com.ujizin.camposer.state.properties.VideoStabilizationMode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CameraVideoStabilizationTest : CameraSessionTest() {
  @Test
  fun test_videoStabilization_standard() =
    runTest {
      initCameraSession()
      cameraSession.controller.setVideoStabilizationEnabled(VideoStabilizationMode.Standard)
      cameraTest.assertVideoStabilization(VideoStabilizationMode.Standard)
      assertEquals(
        VideoStabilizationMode.Standard,
        cameraSession.state.videoStabilizationMode.value,
      )
    }

  @Test
  fun test_videoStabilization_cinematic() =
    runTest {
      initCameraSession()
      cameraSession.controller.setVideoStabilizationEnabled(VideoStabilizationMode.Cinematic)
      cameraTest.assertVideoStabilization(VideoStabilizationMode.Cinematic)
      assertEquals(
        VideoStabilizationMode.Cinematic,
        cameraSession.state.videoStabilizationMode.value,
      )
    }

  @Test
  fun test_videoStabilization_off() =
    runTest {
      initCameraSession()
      cameraSession.controller.setVideoStabilizationEnabled(VideoStabilizationMode.Standard)
      cameraSession.controller.setVideoStabilizationEnabled(VideoStabilizationMode.Off)
      cameraTest.assertVideoStabilization(VideoStabilizationMode.Off)
      assertEquals(VideoStabilizationMode.Off, cameraSession.state.videoStabilizationMode.value)
    }
}
