package com.ujizin.camposer.fake

import com.ujizin.camposer.internal.core.CameraEngineImpl
import com.ujizin.camposer.internal.core.IOSCameraEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
internal actual class FakeCameraEngine actual constructor(
  cameraTest: FakeCameraTest,
  testDispatcher: CoroutineDispatcher,
) : IOSCameraEngine by CameraEngineImpl(
  iOSCameraController = cameraTest.fakeIosCameraController,
  cameraInfo = cameraTest.cameraInfo,
  cameraController = cameraTest.cameraController,
  dispatcher = testDispatcher,
)
