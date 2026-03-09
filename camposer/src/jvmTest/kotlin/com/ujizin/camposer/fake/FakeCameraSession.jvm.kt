package com.ujizin.camposer.fake

import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.internal.record.FrameRecorderBridge
import com.ujizin.camposer.internal.record.JvmAudioCapture
import com.ujizin.camposer.internal.record.JvmVideoRecorder
import com.ujizin.camposer.internal.record.MatFrameConverter
import com.ujizin.camposer.session.CameraSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytedeco.javacv.Frame
import org.bytedeco.opencv.opencv_core.Mat
import java.nio.ShortBuffer

/**
 * A [FrameRecorderBridge] that tracks calls without touching FFmpeg.
 * Its [stop] throws if [shouldThrow] is true, enabling simulation of recording errors.
 */
private class FakeFrameRecorderBridge(
  private val shouldThrow: () -> Boolean = { false },
) : FrameRecorderBridge {
  override var videoCodec: Int = 0
  override var audioCodec: Int = 0
  override var frameRate: Double = 30.0
  override var sampleRate: Int = 44100
  override var audioChannels: Int = 1
  override var videoBitrate: Int = 0
  override var audioBitrate: Int = 0

  override fun start() = Unit

  override fun record(frame: Frame) = Unit

  override fun recordSamples(
    sampleRate: Int,
    audioChannels: Int,
    samples: ShortBuffer,
  ): Boolean = true

  override fun stop() {
    if (shouldThrow()) throw RuntimeException("Simulated recording error")
  }

  override fun release() = Unit
}

private class FakeMatFrameConverter : MatFrameConverter {
  override fun convert(mat: Mat): Frame? = null

  override fun close() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
internal actual fun createCameraSession(
  fakeCameraTest: FakeCameraTest,
  testDispatcher: CoroutineDispatcher,
  autoStart: Boolean,
) = CameraSession(
  cameraEngine = FakeCameraEngine(
    cameraTest = fakeCameraTest,
    testDispatcher = testDispatcher,
  ),
  recordControllerFactory = { engine ->
    DefaultRecordController(
      cameraEngine = engine,
      videoRecorderFactory = { filename, capture ->
        JvmVideoRecorder(
          filename = filename,
          capture = capture,
          recorderFactory = { _, _, _, _ ->
            FakeFrameRecorderBridge(shouldThrow = { fakeCameraTest.hasErrorInRecording })
          },
          converterFactory = { FakeMatFrameConverter() },
        )
      },
      audioFactory = {
        JvmAudioCapture(audioDeviceSelector = NoOpAudioDeviceSelector())
      },
    )
  },
)
