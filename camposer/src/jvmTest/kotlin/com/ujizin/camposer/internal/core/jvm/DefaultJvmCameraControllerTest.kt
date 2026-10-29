package com.ujizin.camposer.internal.core.jvm

import com.ujizin.camposer.fake.FakeJvmAudioCapture
import com.ujizin.camposer.fake.FakeJvmVideoRecorder
import com.ujizin.camposer.fake.FakeJvmVideoSource
import com.ujizin.camposer.internal.error.CameraDeviceNotFoundException
import com.ujizin.camposer.internal.error.CameraNotStreamingException
import com.ujizin.camposer.internal.preview.PreviewGeometry
import com.ujizin.camposer.state.properties.CameraFrame
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ScaleType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultJvmCameraControllerTest {
  // Tests drive the scheduler by hand: runTest would spin forever because the capture loop
  // re-schedules its retry delay while no frame is available.
  private val dispatcher = StandardTestDispatcher()

  private fun runCurrent() = dispatcher.scheduler.runCurrent()

  private fun advanceTimeBy(delayTimeMillis: Long) {
    dispatcher.scheduler.advanceTimeBy(delayTimeMillis)
    dispatcher.scheduler.runCurrent()
  }

  private val recorderFactory = FakeJvmVideoRecorder.Factory()
  private val audioFactory = FakeJvmAudioCapture.Factory()

  private fun createController(vararg sources: FakeJvmVideoSource) =
    DefaultJvmCameraController(
      videoSourceFactory = FakeJvmVideoSource.Factory(*sources),
      recorderFactory = recorderFactory,
      audioCaptureFactory = audioFactory,
      captureDispatcher = dispatcher,
      analysisDispatcher = dispatcher,
    )

  private fun DefaultJvmCameraController.openAndWait(deviceIndex: Int = 0): Result<Unit> {
    var result: Result<Unit>? = null
    open(deviceIndex) { result = it }
    dispatcher.scheduler.runCurrent()
    return assertNotNull(result)
  }

  @Test
  fun test_open_reports_format_and_device_index() {
    val source = FakeJvmVideoSource()
    val controller = createController(source)

    val result = controller.openAndWait()

    assertTrue(result.isSuccess)
    assertEquals(0, controller.deviceIndex)
    assertEquals(0, source.openedIndex)
    val format = assertNotNull(controller.currentFormat)
    assertEquals(1280, format.width)
    assertEquals(720, format.height)
    assertEquals(30, format.maxFps)
  }

  @Test
  fun test_open_missing_device_fails_and_releases_source() {
    val source = FakeJvmVideoSource(availableIndexes = emptySet())
    val controller = createController(source)

    val result = controller.openAndWait(deviceIndex = 3)

    assertIs<CameraDeviceNotFoundException>(result.exceptionOrNull())
    assertNull(controller.deviceIndex)
    assertTrue(source.isReleased)
  }

  @Test
  fun test_open_again_releases_previous_source() {
    val first = FakeJvmVideoSource(availableIndexes = setOf(0))
    val second = FakeJvmVideoSource(availableIndexes = setOf(1))
    val controller = createController(first, second)

    controller.openAndWait(0)
    controller.openAndWait(1)

    assertTrue(first.isReleased)
    assertEquals(1, second.openedIndex)
    assertEquals(1, controller.deviceIndex)
  }

  @Test
  fun test_streaming_publishes_frames() {
    val source = FakeJvmVideoSource(framesToDeliver = 2)
    val controller = createController(source)
    controller.openAndWait()

    controller.startStreaming()
    runCurrent()

    assertTrue(controller.isStreaming.value)
    assertNotNull(controller.currentFrame.value)
    assertEquals(2, source.framesRead)
  }

  @Test
  fun test_streaming_requested_before_open_starts_after_open() {
    val source = FakeJvmVideoSource(framesToDeliver = 1)
    val controller = createController(source)

    controller.startStreaming()
    runCurrent()
    assertFalse(controller.isStreaming.value)

    controller.openAndWait()
    runCurrent()

    assertTrue(controller.isStreaming.value)
  }

  @Test
  fun test_stop_streaming_ends_loop() {
    val source = FakeJvmVideoSource(framesToDeliver = 1)
    val controller = createController(source)
    controller.openAndWait()
    controller.startStreaming()
    runCurrent()

    controller.stopStreaming()
    advanceTimeBy(100)
    runCurrent()

    assertFalse(controller.isStreaming.value)
  }

  @Test
  fun test_take_picture_uses_quality_and_mirror() {
    val source = FakeJvmVideoSource(framesToDeliver = 1)
    val controller = createController(source)
    controller.openAndWait()
    controller.setPictureQuality(42)
    controller.startStreaming()
    runCurrent()

    var result: Result<ByteArray>? = null
    controller.takePicture(isMirrored = true) { result = it }
    source.framesToDeliver = 2
    advanceTimeBy(100)
    runCurrent()

    assertContentEquals(byteArrayOf(42), assertNotNull(result).getOrNull())
    assertEquals(42, source.frame.jpegQuality)
    assertEquals(true, source.frame.wasJpegMirrored)
  }

  @Test
  fun test_take_picture_to_file_writes_bytes() {
    val source = FakeJvmVideoSource(framesToDeliver = 1)
    val controller = createController(source)
    controller.openAndWait()
    controller.setPictureQuality(7)
    controller.startStreaming()
    runCurrent()
    val file = File.createTempFile("camposer", ".jpg").apply { delete() }

    var result: Result<String>? = null
    controller.takePicture(file.path, isMirrored = false) { result = it }
    source.framesToDeliver = 2
    advanceTimeBy(100)
    runCurrent()

    assertEquals(file.path, assertNotNull(result).getOrNull())
    assertContentEquals(byteArrayOf(7), file.readBytes())
    file.delete()
  }

  @Test
  fun test_take_picture_before_streaming_fails() {
    val controller = createController(FakeJvmVideoSource())
    controller.openAndWait()

    var result: Result<ByteArray>? = null
    controller.takePicture(isMirrored = false) { result = it }

    assertIs<CameraNotStreamingException>(assertNotNull(result).exceptionOrNull())
  }

  @Test
  fun test_analyzer_receives_frame_with_preview_geometry() {
    val source = FakeJvmVideoSource(framesToDeliver = 1)
    val controller = createController(source)
    controller.openAndWait()
    val geometry = assertNotNull(
      PreviewGeometry.compute(4, 2, 40F, 20F, ScaleType.FitCenter, false, 1F),
    )
    controller.updatePreviewGeometry(geometry)
    val frames = mutableListOf<CameraFrame>()
    controller.setFrameAnalyzer(ImageAnalyzer.Analyzer { frames += it })

    controller.startStreaming()
    runCurrent()

    val frame = assertNotNull(frames.singleOrNull())
    assertEquals(4, frame.width)
    assertEquals(2, frame.height)
    assertEquals(10F, frame.toPreviewPosition(1F, 1F).x)
  }

  @Test
  fun test_analyzer_is_cleared() {
    val source = FakeJvmVideoSource(framesToDeliver = 1)
    val controller = createController(source)
    controller.openAndWait()
    var analyzed = 0
    controller.setFrameAnalyzer(ImageAnalyzer.Analyzer { analyzed++ })
    controller.setFrameAnalyzer(null)

    controller.startStreaming()
    runCurrent()

    assertEquals(0, analyzed)
  }

  @Test
  fun test_set_format_and_frame_rate_update_current_format() {
    val source = FakeJvmVideoSource()
    val controller = createController(source)
    controller.openAndWait()

    controller.setFormat(640, 480)
    controller.setFrameRate(15)

    val format = assertNotNull(controller.currentFormat)
    assertEquals(640, format.width)
    assertEquals(480, format.height)
    assertEquals(15, format.maxFps)
    assertTrue(source.setCalls.contains(JvmVideoProperty.FrameWidth to 640.0))
    assertTrue(source.setCalls.contains(JvmVideoProperty.FrameRate to 15.0))
  }

  @Test
  fun test_exposure_supported_on_log_scale_backend() {
    val source = FakeJvmVideoSource(backendName = "MSMF").apply {
      properties[JvmVideoProperty.Exposure] = -6.0
    }
    val controller = createController(source)
    controller.openAndWait()

    assertTrue(controller.capabilities.isExposureSupported)
    assertEquals(-3F, controller.capabilities.minExposure)
    assertEquals(3F, controller.capabilities.maxExposure)

    controller.setExposureCompensation(1F)
    assertTrue(source.setCalls.contains(JvmVideoProperty.AutoExposure to 0.0))
    assertTrue(source.setCalls.contains(JvmVideoProperty.Exposure to -5.0))

    controller.setExposureCompensation(0F)
    assertEquals(JvmVideoProperty.AutoExposure to 1.0, source.setCalls.last())
  }

  @Test
  fun test_exposure_unsupported_on_avfoundation() {
    val source = FakeJvmVideoSource(backendName = "AVFOUNDATION")
    val controller = createController(source)
    controller.openAndWait()

    controller.setExposureCompensation(2F)

    assertFalse(controller.capabilities.isExposureSupported)
    assertTrue(source.setCalls.none { (property, _) -> property == JvmVideoProperty.Exposure })
  }

  @Test
  fun test_start_recording_before_streaming_fails() {
    val controller = createController(FakeJvmVideoSource())
    controller.openAndWait()

    var result: Result<String>? = null
    controller.startRecording("/tmp/a.mp4", isMirrored = false) { result = it }

    assertIs<CameraNotStreamingException>(assertNotNull(result).exceptionOrNull())
  }

  @Test
  fun test_recording_receives_frames_until_stopped() {
    val source = FakeJvmVideoSource(framesToDeliver = 1)
    val controller = createController(source)
    controller.openAndWait()
    controller.startStreaming()
    runCurrent()

    var result: Result<String>? = null
    controller.startRecording("/tmp/a.mp4", isMirrored = true) { result = it }
    assertTrue(controller.isRecording.value)
    assertEquals(1280, recorderFactory.last.config.width)
    assertEquals(30.0, recorderFactory.last.config.frameRate)

    source.framesToDeliver = 3
    advanceTimeBy(100)
    runCurrent()
    assertEquals(2, recorderFactory.last.frames.size)
    assertTrue(recorderFactory.last.frames.all { it.isMirrored })

    assertTrue(controller.stopRecording().isSuccess)
    assertEquals("/tmp/a.mp4", assertNotNull(result).getOrNull())
    assertFalse(controller.isRecording.value)
  }

  @Test
  fun test_release_releases_source_and_clears_frame() {
    val source = FakeJvmVideoSource(framesToDeliver = 1)
    val controller = createController(source)
    controller.openAndWait()
    controller.startStreaming()
    runCurrent()

    controller.release()
    runCurrent()

    assertTrue(source.isReleased)
    assertNull(controller.currentFrame.value)
    assertFalse(controller.isStreaming.value)
  }
}
