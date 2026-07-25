package com.ujizin.camposer.session

import com.ujizin.camposer.fake.FakeCameraEngine
import com.ujizin.camposer.fake.FakeCameraTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class CameraSessionPermissionTest {
  private val testDispatcher = StandardTestDispatcher()
  private val cameraTest = FakeCameraTest(testDispatcher)

  private fun createSession(): CameraSession =
    CameraSession(
      cameraEngine = FakeCameraEngine(
        cameraTest = cameraTest,
        testDispatcher = testDispatcher,
      ),
    )

  @Test
  fun test_initialization_fails_when_camera_permission_denied() {
    cameraTest.fakeIosCameraController.fakeIsCameraAuthorized = false

    val cameraSession = createSession()

    assertFalse(cameraSession.isInitialized)
    assertTrue(cameraSession.hasInitializationError)
  }

  @Test
  fun test_retry_initialization_succeeds_after_permission_granted() {
    cameraTest.fakeIosCameraController.fakeIsCameraAuthorized = false
    val cameraSession = createSession()
    assertTrue(cameraSession.hasInitializationError)

    cameraTest.fakeIosCameraController.fakeIsCameraAuthorized = true
    cameraSession.retryInitialization()

    assertTrue(cameraSession.isInitialized)
    assertFalse(cameraSession.hasInitializationError)
  }
}
