package com.ujizin.camposer

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.ujizin.camposer.manager.CameraDeviceState
import com.ujizin.camposer.manager.CameraDevicesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
internal class CameraDevicesManagerTest {
  private val context: Context
    get() = InstrumentationRegistry.getInstrumentation().targetContext

  @Test
  fun test_initial_state_is_initial() =
    runTest {
      val manager = CameraDevicesManager(context)
      try {
        assertEquals(CameraDeviceState.Initial, manager.cameraDevicesState.value)
        awaitInit(manager)
      } finally {
        manager.release()
      }
    }

  @Test
  fun test_state_transitions_to_devices_after_init() =
    runTest {
      val manager = CameraDevicesManager(context)
      try {
        val state = awaitInit(manager)
        assertTrue(state is CameraDeviceState.Devices)
      } finally {
        manager.release()
      }
    }

  @Test
  fun test_devices_state_contains_at_least_one_camera() =
    runTest {
      val manager = CameraDevicesManager(context)
      try {
        val state = awaitInit(manager) as CameraDeviceState.Devices
        assertTrue(state.cameraDevices.isNotEmpty())
      } finally {
        manager.release()
      }
    }

  @Test
  fun test_release_does_not_throw() =
    runTest {
      val manager = CameraDevicesManager(context)
      try {
        awaitInit(manager)
      } finally {
        manager.release()
      }
    }

  private suspend fun awaitInit(manager: CameraDevicesManager): CameraDeviceState =
    withContext(Dispatchers.Default) {
      withTimeout(INIT_TIMEOUT) {
        manager.cameraDevicesState.first { it !is CameraDeviceState.Initial }
      }
    }

  private companion object {
    private const val INIT_TIMEOUT = 10_000L
  }
}
