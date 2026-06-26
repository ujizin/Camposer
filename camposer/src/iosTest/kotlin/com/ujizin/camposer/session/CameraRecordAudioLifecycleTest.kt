package com.ujizin.camposer.session

import com.ujizin.camposer.state.properties.CaptureMode
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CameraRecordAudioLifecycleTest : CameraSessionTest() {
  @Test
  fun test_audio_input_is_enabled_when_switching_to_video_mode() {
    assertFalse(cameraTest.fakeIosCameraController.fakeAudioEnabled)

    updateSession(captureMode = CaptureMode.Video)

    assertTrue(cameraTest.fakeIosCameraController.fakeAudioEnabled)
  }

  @Test
  fun test_audio_input_is_disabled_when_switching_to_image_mode() {
    updateSession(captureMode = CaptureMode.Video)

    assertTrue(cameraTest.fakeIosCameraController.fakeAudioEnabled)

    updateSession(captureMode = CaptureMode.Image)

    assertFalse(cameraTest.fakeIosCameraController.fakeAudioEnabled)
  }

  @Test
  fun test_audio_stays_enabled_during_recording_lifecycle() {
    updateSession(captureMode = CaptureMode.Video)

    assertTrue(cameraTest.fakeIosCameraController.fakeAudioEnabled)

    controller.startRecording("/video/video.mp4") { }

    assertTrue(cameraTest.fakeIosCameraController.fakeAudioEnabled)

    controller.stopRecording()

    assertTrue(cameraTest.fakeIosCameraController.fakeAudioEnabled)
  }

  @Test
  fun test_mute_recording_returns_failure_on_ios() {
    updateSession(captureMode = CaptureMode.Video)

    val result = controller.muteRecording(true)

    assertTrue(result.isFailure)
    assertFalse(controller.isMuted.value)
  }
}
