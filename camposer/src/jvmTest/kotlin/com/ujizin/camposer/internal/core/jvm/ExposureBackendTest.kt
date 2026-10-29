package com.ujizin.camposer.internal.core.jvm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ExposureBackendTest {
  @Test
  fun test_from_backend_name_matches_known_backends() {
    assertEquals(ExposureBackend.V4L2, ExposureBackend.fromBackendName("V4L2"))
    assertEquals(ExposureBackend.MSMF, ExposureBackend.fromBackendName("MSMF"))
    assertEquals(ExposureBackend.DSHOW, ExposureBackend.fromBackendName("dshow"))
  }

  @Test
  fun test_from_backend_name_returns_null_for_unsupported_backends() {
    assertNull(ExposureBackend.fromBackendName("AVFOUNDATION"))
    assertNull(ExposureBackend.fromBackendName(""))
  }

  @Test
  fun test_log_scale_backend_adds_stops() {
    assertEquals(-5.0, ExposureBackend.MSMF.toRawExposure(baseExposure = -6.0, compensation = 1F))
    assertEquals(-8.0, ExposureBackend.DSHOW.toRawExposure(baseExposure = -6.0, compensation = -2F))
  }

  @Test
  fun test_linear_backend_doubles_per_stop() {
    assertEquals(200.0, ExposureBackend.V4L2.toRawExposure(baseExposure = 100.0, compensation = 1F))
    assertEquals(25.0, ExposureBackend.V4L2.toRawExposure(baseExposure = 100.0, compensation = -2F))
  }
}
