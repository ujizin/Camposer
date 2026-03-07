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
  fun `initial state is Initial`() {
    // Use a very long poll interval to avoid racing
    val manager =
      CameraDevicesManager(
        dispatcher = Dispatchers.Unconfined,
        pollIntervalMs = Long.MAX_VALUE,
        cameraDeviceDiscoverer = FakeCameraDeviceDiscoverer(CameraDeviceState.Initial),
      )
    // After construction, before any poll, state should be Initial
    assertIs<CameraDeviceState.Initial>(manager.cameraDevicesState.value)
    manager.release()
  }

  @Test
  fun `release does not throw`() {
    val manager =
      CameraDevicesManager(
        dispatcher = Dispatchers.Unconfined,
        pollIntervalMs = Long.MAX_VALUE,
        cameraDeviceDiscoverer = FakeCameraDeviceDiscoverer(CameraDeviceState.Initial),
      )
    manager.release()
  }

  @Test
  fun `poll updates state to Devices when discoverer returns cameras`() =
    runTest {
      val dispatcher = StandardTestDispatcher(testScheduler)
      val expected = CameraDeviceState.Devices(listOf(fakeCameraDevice(id = "0")))
      val manager = CameraDevicesManager(
        dispatcher = dispatcher,
        pollIntervalMs = 1000L,
        cameraDeviceDiscoverer = FakeCameraDeviceDiscoverer(expected),
      )

      runCurrent()

      assertEquals(expected, manager.cameraDevicesState.value)
      manager.release()
    }

  @Test
  fun `poll updates state back to Initial when discoverer result changes`() =
    runTest {
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

      runCurrent()
      assertIs<CameraDeviceState.Devices>(manager.cameraDevicesState.value)

      advanceTimeBy(1_000L)
      runCurrent()
      assertIs<CameraDeviceState.Initial>(manager.cameraDevicesState.value)
      manager.release()
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
