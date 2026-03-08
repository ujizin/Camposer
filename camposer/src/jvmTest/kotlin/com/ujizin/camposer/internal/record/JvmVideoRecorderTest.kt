package com.ujizin.camposer.internal.record

import com.ujizin.camposer.internal.capture.FakeJvmCameraCapture
import org.bytedeco.javacv.Frame
import org.bytedeco.opencv.opencv_core.Mat
import java.nio.ShortBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class JvmVideoRecorderTest {
  // ---------------------------------------------------------------------------
  // Fake FrameRecorderBridge
  // ---------------------------------------------------------------------------

  private class FakeFrameRecorderBridge : FrameRecorderBridge {
    var startCalled: Boolean = false
      private set
    var recordCalled: Boolean = false
      private set
    var recordSamplesCalled: Boolean = false
      private set
    var stopCalled: Boolean = false
      private set
    var releaseCalled: Boolean = false
      private set

    var capturedFrameRate: Double = 0.0

    override var videoCodec: Int = 0
    override var audioCodec: Int = 0
    override var frameRate: Double
      get() = capturedFrameRate
      set(value) {
        capturedFrameRate = value
      }
    override var sampleRate: Int = 0
    override var audioChannels: Int = 0
    override var videoBitrate: Int = 0
    override var audioBitrate: Int = 0

    override fun start() {
      startCalled = true
    }

    override fun record(frame: Frame) {
      recordCalled = true
    }

    override fun recordSamples(
      sampleRate: Int,
      audioChannels: Int,
      samples: ShortBuffer,
    ): Boolean {
      recordSamplesCalled = true
      return true
    }

    override fun stop() {
      stopCalled = true
    }

    override fun release() {
      releaseCalled = true
    }
  }

  // ---------------------------------------------------------------------------
  // Fake MatFrameConverter — avoids native OpenCV calls in tests
  // ---------------------------------------------------------------------------

  private class FakeMatFrameConverter : MatFrameConverter {
    private val fakeFrame = Frame()

    override fun convert(mat: Mat): Frame = fakeFrame

    override fun close() = Unit
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private fun buildRecorder(
    capture: FakeJvmCameraCapture = FakeJvmCameraCapture(),
    fake: FakeFrameRecorderBridge = FakeFrameRecorderBridge(),
  ): JvmVideoRecorder =
    JvmVideoRecorder(
      filename = "test.mp4",
      capture = capture,
      recorderFactory = { _, _, _, _ -> fake },
      converterFactory = { FakeMatFrameConverter() },
    )

  // ---------------------------------------------------------------------------
  // Tests
  // ---------------------------------------------------------------------------

  @Test
  fun `given recorder started when record called then frame is passed to recorder bridge`() {
    // Given
    val fake = FakeFrameRecorderBridge()
    val recorder = buildRecorder(fake = fake)
    recorder.start()

    // When
    recorder.record(Mat())

    // Then
    assertTrue(fake.startCalled)
    assertTrue(fake.recordCalled)
  }

  @Test
  fun `given recorder not started when record called then no frame is passed`() {
    // Given
    val fake = FakeFrameRecorderBridge()
    val recorder = buildRecorder(fake = fake)

    // When — no start() called
    recorder.record(Mat())

    // Then
    assertFalse(fake.recordCalled)
  }

  @Test
  fun `given recorder started when stop called then recorder stop and release are called`() {
    // Given
    val fake = FakeFrameRecorderBridge()
    val recorder = buildRecorder(fake = fake)
    recorder.start()

    // When
    recorder.stop()

    // Then
    assertTrue(fake.stopCalled)
    assertTrue(fake.releaseCalled)
  }

  @Test
  fun `given capture returns zero FPS when starting then defaults to 30 FPS`() {
    // Given
    val fake = FakeFrameRecorderBridge()
    val recorder =
      JvmVideoRecorder(
        filename = "test.mp4",
        capture = FakeJvmCameraCapture(fpsOverride = 0.0),
        recorderFactory = { _, _, _, _ -> fake },
        converterFactory = { FakeMatFrameConverter() },
      )

    // When
    recorder.start()

    // Then
    assertEquals(30.0, fake.capturedFrameRate)
  }

  @Test
  fun `given recorder started when recordSamples called then samples are forwarded to bridge`() {
    val fake = FakeFrameRecorderBridge()
    val recorder = buildRecorder(fake = fake)
    recorder.start()
    recorder.recordSamples(ShortBuffer.wrap(ShortArray(4) { 1 }))
    assertTrue(fake.recordSamplesCalled)
  }

  @Test
  fun `given recorder not started when recordSamples called then no samples are forwarded`() {
    val fake = FakeFrameRecorderBridge()
    val recorder = buildRecorder(fake = fake)
    recorder.recordSamples(ShortBuffer.wrap(ShortArray(4) { 1 }))
    assertFalse(fake.recordSamplesCalled)
  }
}
