package com.ujizin.camposer.fake

import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.internal.record.AudioDeviceSelector
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
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.Control
import javax.sound.sampled.Line
import javax.sound.sampled.LineListener
import javax.sound.sampled.TargetDataLine

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

private class NoOpTargetDataLine : TargetDataLine {
  override fun open(format: AudioFormat) = Unit

  override fun open() = Unit

  override fun open(
    format: AudioFormat,
    bufferSize: Int,
  ) = Unit

  override fun start() = Unit

  override fun stop() = Unit

  override fun close() = Unit

  override fun drain() = Unit

  override fun flush() = Unit

  override fun isRunning(): Boolean = false

  override fun isActive(): Boolean = false

  override fun getFormat(): AudioFormat = AudioFormat(44100f, 16, 1, true, false)

  override fun getBufferSize(): Int = 1024

  override fun available(): Int = 0

  override fun getFramePosition(): Int = 0

  override fun getLongFramePosition(): Long = 0L

  override fun getMicrosecondPosition(): Long = 0L

  override fun getLevel(): Float = 0f

  override fun isOpen(): Boolean = false

  override fun getControls(): Array<Control> = emptyArray()

  override fun isControlSupported(control: Control.Type): Boolean = false

  override fun getControl(control: Control.Type): Control = throw UnsupportedOperationException()

  override fun addLineListener(listener: LineListener) = Unit

  override fun removeLineListener(listener: LineListener) = Unit

  override fun getLineInfo(): Line.Info = Line.Info(TargetDataLine::class.java)

  override fun read(
    b: ByteArray,
    off: Int,
    len: Int,
  ): Int = 0
}

private class NoOpAudioDeviceSelector : AudioDeviceSelector {
  override fun openLine(format: AudioFormat): TargetDataLine = NoOpTargetDataLine()
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
