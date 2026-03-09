package com.ujizin.camposer.session

import com.ujizin.camposer.state.properties.CaptureMode
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CameraRecordAudioLifecycleTest : CameraSessionTest() {
  @Test
  fun test_audio_input_is_only_enabled_while_recording() {
    updateSession(captureMode = CaptureMode.Video)

    assertFalse(cameraTest.fakeIosCameraController.fakeAudioEnabled)

    controller.startRecording("/video/video.mp4") { }

    assertTrue(cameraTest.fakeIosCameraController.fakeAudioEnabled)

    controller.stopRecording()

    assertFalse(cameraTest.fakeIosCameraController.fakeAudioEnabled)
  }

  @Test
  fun test_preconfigured_mute_is_preserved_when_recording_starts() {
    updateSession(captureMode = CaptureMode.Video)

    controller.muteRecording(true)

    assertTrue(controller.isMuted.value)

    controller.startRecording("/video/video.mp4") { }

    assertTrue(controller.isMuted.value)
    assertFalse(cameraTest.fakeIosCameraController.fakeAudioEnabled)

    controller.muteRecording(false)

    assertFalse(controller.isMuted.value)
    assertTrue(cameraTest.fakeIosCameraController.fakeAudioEnabled)
  }
}
