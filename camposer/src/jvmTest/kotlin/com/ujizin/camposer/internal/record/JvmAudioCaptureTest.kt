package com.ujizin.camposer.internal.record

import kotlinx.coroutines.test.runTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.Control
import javax.sound.sampled.Line
import javax.sound.sampled.LineListener
import javax.sound.sampled.TargetDataLine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class JvmAudioCaptureTest {
  // ---------------------------------------------------------------------------
  // Fakes
  // ---------------------------------------------------------------------------

  private open class FakeTargetDataLine(
    private val bytesToReturn: ByteArray = ByteArray(0),
    // After this many reads the fake returns 0, letting the coroutine loop yield without
    // forwarding samples. Use Int.MAX_VALUE to always return bytes.
    private val maxReads: Int = Int.MAX_VALUE,
  ) : TargetDataLine {
    var stopCalled: Boolean = false
      private set
    var closeCalled: Boolean = false
      private set
    var openCalled: Boolean = false
      private set
    private var readCount: Int = 0

    override fun open(format: AudioFormat) {
      openCalled = true
    }

    override fun start() = Unit

    override fun stop() {
      stopCalled = true
    }

    override fun close() {
      closeCalled = true
    }

    open override fun read(
      b: ByteArray,
      off: Int,
      len: Int,
    ): Int {
      if (bytesToReturn.isEmpty() || readCount >= maxReads) return 0
      readCount++
      val count = minOf(len, bytesToReturn.size)
      bytesToReturn.copyInto(b, off, 0, count)
      return count
    }

    override fun getBufferSize(): Int = bytesToReturn.size.coerceAtLeast(10) * 5

    // Unimplemented TargetDataLine / DataLine / Line methods
    override fun open() = throw UnsupportedOperationException()

    override fun open(
      format: AudioFormat,
      bufferSize: Int,
    ) = throw UnsupportedOperationException()

    override fun drain() = throw UnsupportedOperationException()

    override fun flush() = throw UnsupportedOperationException()

    override fun isRunning(): Boolean = throw UnsupportedOperationException()

    override fun isActive(): Boolean = throw UnsupportedOperationException()

    override fun getFormat(): AudioFormat = throw UnsupportedOperationException()

    override fun getFramePosition(): Int = throw UnsupportedOperationException()

    override fun getLongFramePosition(): Long = throw UnsupportedOperationException()

    override fun getMicrosecondPosition(): Long = throw UnsupportedOperationException()

    override fun getLevel(): Float = throw UnsupportedOperationException()

    override fun isOpen(): Boolean = throw UnsupportedOperationException()

    override fun getControls(): Array<Control> = throw UnsupportedOperationException()

    override fun isControlSupported(control: Control.Type): Boolean =
      throw UnsupportedOperationException()

    override fun getControl(control: Control.Type): Control = throw UnsupportedOperationException()

    override fun addLineListener(listener: LineListener) = throw UnsupportedOperationException()

    override fun removeLineListener(listener: LineListener) = throw UnsupportedOperationException()

    override fun getLineInfo(): Line.Info = throw UnsupportedOperationException()

    override fun available(): Int = throw UnsupportedOperationException()
  }

  private class FakeAudioDeviceSelector(
    private val line: FakeTargetDataLine? = null,
    private val throwOnOpen: Boolean = false,
  ) : AudioDeviceSelector {
    override fun openLine(format: AudioFormat): TargetDataLine {
      if (throwOnOpen) throw IllegalStateException("audio device unavailable")
      return line ?: FakeTargetDataLine()
    }
  }

  // ---------------------------------------------------------------------------
  // Tests
  // ---------------------------------------------------------------------------

  @Test
  fun `given capture started when mute is toggled then isMuted state updates`() =
    runTest {
      // Given
      val fakeLine = FakeTargetDataLine()
      val selector = FakeAudioDeviceSelector(fakeLine)
      val capture = JvmAudioCapture(audioDeviceSelector = selector)

      capture.start(this, onSamples = {})

      // When / Then: mute on
      capture.mute(true)
      assertTrue(capture.isMuted.value)

      // When / Then: mute off
      capture.mute(false)
      assertFalse(capture.isMuted.value)

      capture.stop()
    }

  @Test
  fun `given capture started when muted then samples are not forwarded to recorder`() =
    runTest {
      // Gate that lets us pause the IO thread between reads for precise control.
      // The fake blocks on gateRead until the test releases it.
      val gateRead = CountDownLatch(1)
      val sampleBytes = byteArrayOf(0x01, 0x00, 0x02, 0x00)
      // Returns bytes on the first read; blocks on subsequent reads until gateRead opens.
      val blockingLine =
        object : FakeTargetDataLine(bytesToReturn = sampleBytes, maxReads = 1) {
          override fun read(
            b: ByteArray,
            off: Int,
            len: Int,
          ): Int {
            val n = super.read(b, off, len)
            if (n <= 0) gateRead.await(5, TimeUnit.SECONDS) // park after first read
            return n
          }
        }

      val sampleDelivered = CountDownLatch(1)
      var onSamplesCount = 0
      val capture = JvmAudioCapture(audioDeviceSelector = FakeAudioDeviceSelector(blockingLine))

      // Unmuted: start on the real IO dispatcher; the coroutine delivers one sample then blocks.
      capture.start(backgroundScope) {
        onSamplesCount++
        sampleDelivered.countDown()
      }

      // Wait until at least one sample has been delivered before muting.
      assertTrue(sampleDelivered.await(5, TimeUnit.SECONDS), "Timed out waiting for first sample")
      assertTrue(onSamplesCount > 0, "Expected samples to be delivered before muting")

      // Mute and release the gate so the IO thread unblocks and runs another cycle.
      val countBeforeMute = onSamplesCount
      capture.mute(true)
      gateRead.countDown() // release the blocked read(); the next cycle sees isMuted == true

      capture.stop()
      assertEquals(countBeforeMute, onSamplesCount)
    }

  @Test
  fun `given capture started when stop called then line is stopped and closed`() =
    runTest {
      // Given
      val fakeLine = FakeTargetDataLine()
      val selector = FakeAudioDeviceSelector(fakeLine)
      val capture = JvmAudioCapture(audioDeviceSelector = selector)

      capture.start(backgroundScope, onSamples = {})

      // When
      capture.stop()

      // Then: full open → stop → close lifecycle verified
      assertTrue(fakeLine.openCalled)
      assertTrue(fakeLine.stopCalled)
      assertTrue(fakeLine.closeCalled)
    }

  @Test
  fun `given line open fails when start called then exception propagates`() =
    runTest {
      // Given
      val selector = FakeAudioDeviceSelector(throwOnOpen = true)
      val capture = JvmAudioCapture(audioDeviceSelector = selector)

      // When / Then
      assertFailsWith<IllegalStateException> {
        capture.start(this, onSamples = {})
      }
    }
}
