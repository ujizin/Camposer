package com.ujizin.camposer.manager

import com.ujizin.camposer.state.properties.selector.CamLensType
import com.ujizin.camposer.state.properties.selector.CamPosition
import com.ujizin.camposer.state.properties.selector.CameraId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
internal class CameraDevicesManagerJvmTest {
  private val device = CameraDevice(
    cameraId = CameraId(0),
    name = "Camera 0",
    position = CamPosition.Unknown,
    fov = 0F,
    lensType = listOf(CamLensType.Wide),
    photoData = emptyList(),
    videoData = emptyList(),
  )

  @Test
  fun test_state_starts_initial_then_emits_devices() =
    runTest {
      val dispatcher = StandardTestDispatcher(testScheduler)
      val manager = CameraDevicesManager(
        discoverer = { listOf(device) },
        dispatcher = dispatcher,
      )

      assertEquals(CameraDeviceState.Initial, manager.cameraDevicesState.value)

      dispatcher.scheduler.runCurrent()

      val state = assertIs<CameraDeviceState.Devices>(manager.cameraDevicesState.value)
      assertEquals(listOf(device), state.cameraDevices)
      manager.release()
    }

  @Test
  fun test_release_cancels_pending_discovery() =
    runTest {
      val dispatcher = StandardTestDispatcher(testScheduler)
      val manager = CameraDevicesManager(
        discoverer = { listOf(device) },
        dispatcher = dispatcher,
      )

      manager.release()
      dispatcher.scheduler.runCurrent()

      assertEquals(CameraDeviceState.Initial, manager.cameraDevicesState.value)
    }
}
