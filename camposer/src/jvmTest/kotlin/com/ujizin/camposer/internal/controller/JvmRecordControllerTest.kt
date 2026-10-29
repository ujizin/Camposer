package com.ujizin.camposer.internal.controller

import com.ujizin.camposer.error.RecordNotInitializedException
import com.ujizin.camposer.fake.FakeJvmAudioCapture
import com.ujizin.camposer.fake.FakeJvmFrame
import com.ujizin.camposer.fake.FakeJvmVideoRecorder
import com.ujizin.camposer.internal.error.RecordingInProgressException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class JvmRecordControllerTest {
  private val recorderFactory = FakeJvmVideoRecorder.Factory()
  private val audioFactory = FakeJvmAudioCapture.Factory()
  private var nowNanos = 0L

  private fun createController(
    recorderFactory: FakeJvmVideoRecorder.Factory = this.recorderFactory,
    audioFactory: FakeJvmAudioCapture.Factory = this.audioFactory,
  ) = JvmRecordController(
    recorderFactory = recorderFactory,
    audioCaptureFactory = audioFactory,
    clock = { nowNanos },
  )

  private fun JvmRecordController.startDefault(onVideoCaptured: (Result<String>) -> Unit = {}) =
    start(
      filename = FILENAME,
      width = 640,
      height = 480,
      frameRate = 30.0,
      isMirrored = true,
      onVideoCaptured = onVideoCaptured,
    )

  @Test
  fun test_start_opens_audio_and_recorder() {
    val controller = createController()

    controller.startDefault()

    assertTrue(controller.isRecording.value)
    assertTrue(audioFactory.last.isStarted)
    assertTrue(recorderFactory.last.isStarted)
    assertEquals(1, recorderFactory.last.config.audioChannels)
    assertEquals(44_100, recorderFactory.last.config.sampleRate)
  }

  @Test
  fun test_start_without_microphone_records_video_only() {
    val controller =
      createController(audioFactory = FakeJvmAudioCapture.Factory(failOnStart = true))

    controller.startDefault()

    assertTrue(controller.isRecording.value)
    assertEquals(0, recorderFactory.last.config.audioChannels)
  }

  @Test
  fun test_start_twice_fails_second_request() {
    val controller = createController()
    var result: Result<String>? = null

    controller.startDefault()
    controller.startDefault { result = it }

    assertIs<RecordingInProgressException>(assertNotNull(result).exceptionOrNull())
    assertEquals(1, recorderFactory.created.size)
  }

  @Test
  fun test_recorder_start_failure_reports_error_and_stops_audio() {
    val controller =
      createController(recorderFactory = FakeJvmVideoRecorder.Factory(failOnStart = true))
    var result: Result<String>? = null

    controller.startDefault { result = it }

    assertTrue(assertNotNull(result).isFailure)
    assertFalse(controller.isRecording.value)
    assertTrue(audioFactory.last.isStopped)
  }

  @Test
  fun test_frames_are_recorded_with_elapsed_timestamps() {
    val controller = createController()
    controller.startDefault()

    nowNanos = 1_000_000L
    controller.onFrame(FakeJvmFrame())
    nowNanos = 2_000_000L
    controller.onFrame(FakeJvmFrame())

    val frames = recorderFactory.last.frames
    assertEquals(listOf(1_000L, 2_000L), frames.map { it.timestampMicros })
    assertTrue(frames.all { it.isMirrored })
  }

  @Test
  fun test_pause_skips_frames_and_resume_keeps_timestamps_contiguous() {
    val controller = createController()
    controller.startDefault()

    nowNanos = 1_000_000L
    controller.onFrame(FakeJvmFrame())

    nowNanos = 2_000_000L
    assertTrue(controller.pause().isSuccess)
    nowNanos = 3_000_000L
    controller.onFrame(FakeJvmFrame())
    audioFactory.last.emit(shortArrayOf(1, 2))

    nowNanos = 4_000_000L
    assertTrue(controller.resume().isSuccess)
    nowNanos = 5_000_000L
    controller.onFrame(FakeJvmFrame())

    val recorder = recorderFactory.last
    assertEquals(listOf(1_000L, 3_000L), recorder.frames.map { it.timestampMicros })
    assertTrue(recorder.samples.isEmpty())
  }

  @Test
  fun test_pause_and_resume_without_recording_fail() {
    val controller = createController()

    assertIs<RecordNotInitializedException>(controller.pause().exceptionOrNull())
    assertIs<RecordNotInitializedException>(controller.resume().exceptionOrNull())
  }

  @Test
  fun test_mute_replaces_samples_with_silence() {
    val controller = createController()
    controller.startDefault()

    audioFactory.last.emit(shortArrayOf(7, 8, 9))
    assertTrue(controller.mute(true).isSuccess)
    assertTrue(controller.isMuted.value)
    audioFactory.last.emit(shortArrayOf(7, 8, 9))

    val samples = recorderFactory.last.samples
    assertEquals(2, samples.size)
    assertContentEquals(shortArrayOf(7, 8, 9), samples[0])
    assertContentEquals(shortArrayOf(0, 0, 0), samples[1])
  }

  @Test
  fun test_mute_without_recording_fails() {
    val controller = createController()

    assertIs<RecordNotInitializedException>(controller.mute(true).exceptionOrNull())
    assertFalse(controller.isMuted.value)
  }

  @Test
  fun test_stop_finalizes_recording_and_resets_flags() {
    val controller = createController()
    var result: Result<String>? = null
    controller.startDefault { result = it }
    controller.mute(true)

    val stopResult = controller.stop()

    assertTrue(stopResult.isSuccess)
    assertEquals(FILENAME, assertNotNull(result).getOrNull())
    assertTrue(recorderFactory.last.isStopped)
    assertTrue(audioFactory.last.isStopped)
    assertFalse(controller.isRecording.value)
    assertFalse(controller.isMuted.value)
  }

  @Test
  fun test_stop_without_recording_fails() {
    val controller = createController()

    assertIs<RecordNotInitializedException>(controller.stop().exceptionOrNull())
  }

  @Test
  fun test_stop_failure_is_reported_to_callback() {
    val controller =
      createController(recorderFactory = FakeJvmVideoRecorder.Factory(failOnStop = true))
    var result: Result<String>? = null
    controller.startDefault { result = it }

    val stopResult = controller.stop()

    assertTrue(stopResult.isFailure)
    assertTrue(assertNotNull(result).isFailure)
    assertFalse(controller.isRecording.value)
  }

  @Test
  fun test_frame_failure_ends_recording_with_error() {
    val failingFactory = FakeJvmVideoRecorder.Factory(failOnFrame = true)
    val controller = createController(recorderFactory = failingFactory)
    var result: Result<String>? = null
    controller.startDefault { result = it }

    controller.onFrame(FakeJvmFrame())

    assertTrue(assertNotNull(result).isFailure)
    assertFalse(controller.isRecording.value)
    assertTrue(audioFactory.last.isStopped)
    assertTrue(failingFactory.last.isStopped)
  }

  @Test
  fun test_frames_after_stop_are_ignored() {
    val controller = createController()
    controller.startDefault()
    controller.stop()

    controller.onFrame(FakeJvmFrame())

    assertTrue(recorderFactory.last.frames.isEmpty())
  }

  @Test
  fun test_dispose_stops_active_recording() {
    val controller = createController()
    var result: Result<String>? = null
    controller.startDefault { result = it }

    controller.dispose()

    assertFalse(controller.isRecording.value)
    assertNotNull(result)
  }

  @Test
  fun test_dispose_without_recording_is_noop() {
    val controller = createController()

    controller.dispose()

    assertNull(recorderFactory.created.firstOrNull())
  }

  private companion object {
    const val FILENAME = "/tmp/video.mp4"
  }
}
