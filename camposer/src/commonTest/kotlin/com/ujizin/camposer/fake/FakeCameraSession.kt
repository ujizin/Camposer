package com.ujizin.camposer.fake

import com.ujizin.camposer.session.CameraSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
internal expect fun createCameraSession(
  fakeCameraTest: FakeCameraTest,
  testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher(),
  autoStart: Boolean = true,
): CameraSession
