package com.ujizin.camposer.fake

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.session.CameraSession
import kotlinx.coroutines.CoroutineDispatcher

internal actual fun createCameraSession(
  fakeCameraTest: FakeCameraTest,
  testDispatcher: CoroutineDispatcher,
  autoStart: Boolean,
) = CameraSession(
  controller = CameraController(),
  iosCameraSession = fakeCameraTest.fakeIosCameraController,
  cameraInfo = fakeCameraTest.cameraInfo,
  cameraManagerInternal = FakeCameraManagerInternal(fakeCameraTest, testDispatcher),
).apply {
  if (autoStart) startCamera()
}
