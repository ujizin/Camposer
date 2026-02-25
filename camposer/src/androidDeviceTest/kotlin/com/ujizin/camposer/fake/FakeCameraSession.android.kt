package com.ujizin.camposer.fake

import com.ujizin.camposer.session.CameraSession
import kotlinx.coroutines.CoroutineDispatcher

internal actual fun createCameraSession(
  fakeCameraTest: FakeCameraTest,
  testDispatcher: CoroutineDispatcher,
  autoStart: Boolean,
) = CameraSession(cameraEngine = FakeCameraEngine(fakeCameraTest, testDispatcher))
