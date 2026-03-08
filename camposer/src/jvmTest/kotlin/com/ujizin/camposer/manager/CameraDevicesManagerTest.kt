package com.ujizin.camposer.manager

import com.ujizin.camposer.state.properties.selector.CamLensType
import com.ujizin.camposer.state.properties.selector.CamPosition
import com.ujizin.camposer.state.properties.selector.CameraId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
internal class CameraDevicesManagerTest {
  @Test
  fun `given manager created when no poll has run then initial state is Initial`() {
    // Given
    // Use a very long poll interval to avoid racing.
    val manager =
      CameraDevicesManager(
        dispatcher = Dispatchers.Unconfined,
        pollIntervalMs = Long.MAX_VALUE,
        cameraDeviceDiscoverer = FakeCameraDeviceDiscoverer(CameraDeviceState.Initial),
      )

    // Then
    assertIs<CameraDeviceState.Initial>(manager.cameraDevicesState.value)
    manager.release()
  }

  @Test
  fun `given manager created when release is called then no exception is thrown`() {
    // Given
    val manager =
      CameraDevicesManager(
        dispatcher = Dispatchers.Unconfined,
        pollIntervalMs = Long.MAX_VALUE,
        cameraDeviceDiscoverer = FakeCameraDeviceDiscoverer(CameraDeviceState.Initial),
      )

    // When
    manager.release()

    // Then
    // No exception is thrown.
  }

  @Test
  fun `given discoverer returns devices when polling runs then state becomes Devices`() =
    runTest {
      // Given
      val dispatcher = StandardTestDispatcher(testScheduler)
      val expected = CameraDeviceState.Devices(listOf(fakeCameraDevice(id = "0")))
      val manager = CameraDevicesManager(
        dispatcher = dispatcher,
        pollIntervalMs = 1000L,
        cameraDeviceDiscoverer = FakeCameraDeviceDiscoverer(expected),
      )

      // When
      runCurrent()

      // Then
      assertEquals(expected, manager.cameraDevicesState.value)
      manager.release()
    }

  @Test
  fun `given discoverer result changes when next poll runs then state updates back to Initial`() =
    runTest {
      // Given
      val dispatcher = StandardTestDispatcher(testScheduler)
      var callCount = 0
      val manager = CameraDevicesManager(
        dispatcher = dispatcher,
        pollIntervalMs = 1000L,
        cameraDeviceDiscoverer = CameraDeviceDiscoverer {
          if (callCount++ == 0) CameraDeviceState.Devices(listOf(fakeCameraDevice(id = "1")))
          else CameraDeviceState.Initial
        },
      )

      // When
      runCurrent()

      // Then
      assertIs<CameraDeviceState.Devices>(manager.cameraDevicesState.value)

      // When
      advanceTimeBy(1_000L)
      runCurrent()

      // Then
      assertIs<CameraDeviceState.Initial>(manager.cameraDevicesState.value)
      manager.release()
    }

  @Test
  fun `given manager released when time advances then no further polling occurs`() =
    runTest {
      // Given
      val dispatcher = StandardTestDispatcher(testScheduler)
      var discoverCalls = 0
      val manager = CameraDevicesManager(
        dispatcher = dispatcher,
        pollIntervalMs = 1000L,
        cameraDeviceDiscoverer = CameraDeviceDiscoverer {
          discoverCalls++
          CameraDeviceState.Initial
        },
      )

      // When
      runCurrent()

      // Then
      assertEquals(1, discoverCalls)

      // When
      manager.release()
      advanceTimeBy(1_000L)
      runCurrent()

      // Then
      assertEquals(1, discoverCalls)
    }

  private fun fakeCameraDevice(id: String): CameraDevice =
    CameraDevice(
      cameraId = CameraId(id),
      name = "Fake Camera $id",
      position = CamPosition.External,
      fov = 0f,
      lensType = listOf(CamLensType.Wide),
      photoData = emptyList(),
      videoData = emptyList(),
    )

  private class FakeCameraDeviceDiscoverer(
    private val state: CameraDeviceState,
  ) : CameraDeviceDiscoverer {
    override fun discoverDevices(): CameraDeviceState = state
  }
}
