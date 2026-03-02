package com.ujizin.camposer.manager

import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertIs

internal class CameraDevicesManagerTest {
  @Test
  fun `initial state is Initial`() {
    // Use a very long poll interval to avoid racing
    val manager =
      CameraDevicesManager(dispatcher = Dispatchers.Unconfined, pollIntervalMs = Long.MAX_VALUE)
    // After construction, before any poll, state should be Initial
    assertIs<CameraDeviceState.Initial>(manager.cameraDevicesState.value)
    manager.release()
  }

  @Test
  fun `release does not throw`() {
    val manager =
      CameraDevicesManager(dispatcher = Dispatchers.Unconfined, pollIntervalMs = Long.MAX_VALUE)
    manager.release()
  }
}
