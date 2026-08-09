package com.ujizin.camposer.session

import com.ujizin.camposer.fake.FakeCameraTest
import com.ujizin.camposer.fake.createCameraSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class CameraPendingOperationsTest {
  private val testDispatcher = UnconfinedTestDispatcher()

  private val cameraTest = FakeCameraTest(testDispatcher)

  private val cameraSession by lazy {
    createCameraSession(
      fakeCameraTest = cameraTest,
      testDispatcher = testDispatcher,
      autoStart = false,
    )
  }

  @Test
  fun test_pending_operation_applied_on_session_start() =
    runTest {
      cameraSession.controller.setZoomRatio(3F)

      cameraSession.onSessionStarted()

      cameraTest.assertZoomRatio(3F)
      assertEquals(3F, cameraSession.state.zoomRatio.value)
    }

  @Test
  fun test_last_pending_value_wins_when_set_repeatedly() =
    runTest {
      cameraSession.controller.setZoomRatio(2F)
      cameraSession.controller.setZoomRatio(5F)

      cameraSession.onSessionStarted()

      cameraTest.assertZoomRatio(5F)
      assertEquals(5F, cameraSession.state.zoomRatio.value)
    }

  @Test
  fun test_pending_operations_not_replayed_on_second_session_start() =
    runTest {
      cameraSession.controller.setZoomRatio(4F)

      cameraSession.onSessionStarted()
      cameraSession.controller.setZoomRatio(2F)
      cameraSession.onSessionStarted()

      cameraTest.assertZoomRatio(2F)
      assertEquals(2F, cameraSession.state.zoomRatio.value)
    }

  @Test
  fun test_pending_frame_rate_is_validated_at_replay_time() =
    runTest {
      val outOfRangeFrameRate = 9_999
      val frameRateBeforeStart = cameraSession.state.frameRate.value

      val deferredResult = cameraSession.controller.setVideoFrameRate(outOfRangeFrameRate)

      // Deferred calls cannot validate yet: the range check needs CameraInfo.
      assertTrue(deferredResult.isSuccess)

      cameraSession.onSessionStarted()

      // Validation runs at replay time, so the out-of-range value is never applied.
      assertEquals(frameRateBeforeStart, cameraSession.state.frameRate.value)
    }
}
