package com.ujizin.camposer.manager

import com.ujizin.camposer.fake.FakeJvmVideoSource
import com.ujizin.camposer.internal.core.jvm.JvmVideoProperty
import com.ujizin.camposer.state.properties.selector.CamPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ProbingCameraDeviceDiscovererTest {
  @Test
  fun test_discovers_devices_and_tolerates_one_gap() {
    val sources = listOf(
      FakeJvmVideoSource(availableIndexes = setOf(0)).apply {
        properties[JvmVideoProperty.FrameWidth] = 640.0
        properties[JvmVideoProperty.FrameHeight] = 480.0
        properties[JvmVideoProperty.FrameRate] = 25.0
      },
      FakeJvmVideoSource(availableIndexes = emptySet()),
      FakeJvmVideoSource(availableIndexes = setOf(2)),
      FakeJvmVideoSource(availableIndexes = emptySet()),
      FakeJvmVideoSource(availableIndexes = emptySet()),
    )
    val factory = FakeJvmVideoSource.Factory(*sources.toTypedArray())

    val devices = ProbingCameraDeviceDiscoverer(factory).discoverDevices()

    assertEquals(listOf(0, 2), devices.map { it.cameraId.index })
    assertEquals("Camera 0", devices[0].name)
    assertEquals(CamPosition.Unknown, devices[0].position)
    assertEquals(640, devices[0].photoData.single().width)
    assertEquals(480, devices[0].videoData.single().height)
    assertEquals(25, devices[0].videoData.single().maxFps)
    assertEquals(5, factory.created.size)
    assertTrue(factory.created.all { it.isReleased })
  }

  @Test
  fun test_returns_empty_when_no_device_opens() {
    val factory = FakeJvmVideoSource.Factory(
      FakeJvmVideoSource(availableIndexes = emptySet()),
      FakeJvmVideoSource(availableIndexes = emptySet()),
    )

    val devices = ProbingCameraDeviceDiscoverer(factory).discoverDevices()

    assertTrue(devices.isEmpty())
    assertEquals(2, factory.created.size)
  }

  @Test
  fun test_falls_back_to_default_frame_rate_when_backend_reports_zero() {
    val source = FakeJvmVideoSource(availableIndexes = setOf(0)).apply {
      properties[JvmVideoProperty.FrameRate] = 0.0
    }
    val factory = FakeJvmVideoSource.Factory(
      source,
      FakeJvmVideoSource(availableIndexes = emptySet()),
      FakeJvmVideoSource(availableIndexes = emptySet()),
    )

    val devices = ProbingCameraDeviceDiscoverer(factory).discoverDevices()

    assertEquals(
      30,
      devices
        .single()
        .videoData
        .single()
        .maxFps,
    )
  }
}
