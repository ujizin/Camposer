package com.ujizin.camposer.fake

import com.ujizin.camposer.internal.core.CameraEngineImpl
import com.ujizin.camposer.internal.core.JvmCameraEngine
import kotlinx.coroutines.CoroutineDispatcher

internal actual class FakeCameraEngine actual constructor(
  cameraTest: FakeCameraTest,
  testDispatcher: CoroutineDispatcher,
) : JvmCameraEngine by CameraEngineImpl(
    cameraController = cameraTest.cameraController,
    cameraInfo = cameraTest.cameraInfo,
    jvmCameraController = cameraTest.fakeJvmCameraController,
    dispatcher = testDispatcher,
  ).also(cameraTest::attach)
