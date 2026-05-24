package com.ujizin.camposer

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.ujizin.camposer.manager.CameraDeviceState
import com.ujizin.camposer.manager.CameraDevicesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
  fun test_initial_state_is_initial() {
    val manager = CameraDevicesManager(context)
    assertEquals(CameraDeviceState.Initial, manager.cameraDevicesState.value)
    manager.release()
  }

  @Test
  fun test_state_transitions_to_devices_after_init() {
    val manager = CameraDevicesManager(context)

    val state = runBlocking(Dispatchers.IO) {
      withTimeout(INIT_TIMEOUT) {
        manager.cameraDevicesState.first { it !is CameraDeviceState.Initial }
      }
    }

    assertTrue(state is CameraDeviceState.Devices)
    manager.release()
  }

  @Test
  fun test_devices_state_contains_at_least_one_camera() {
    val manager = CameraDevicesManager(context)

    val state = runBlocking(Dispatchers.IO) {
      withTimeout(INIT_TIMEOUT) {
        manager.cameraDevicesState.first { it !is CameraDeviceState.Initial }
      }
    } as CameraDeviceState.Devices

    assertTrue(state.cameraDevices.isNotEmpty())
    manager.release()
  }

  @Test
  fun test_release_does_not_throw() {
    val manager = CameraDevicesManager(context)
    manager.release()
  }

  private companion object {
    private const val INIT_TIMEOUT = 10_000L
  }
}
