package com.ujizin.camposer

import android.content.res.Configuration
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class ZoomRatioTest : CameraTest() {
  lateinit var zoomRatio: State<Float>

  private val actualCameraXZoom: Float?
    get() = cameraSession.cameraXController.cameraInfo
      ?.zoomState
      ?.value
      ?.zoomRatio

  private lateinit var configurationScreen: Configuration

  private val Offset.Companion.middle
    get() = with(configurationScreen) { Offset(screenWidthDp / 2F, screenHeightDp / 2F) }

  private val Offset.Companion.end
    get() = with(configurationScreen) {
      Offset(screenWidthDp.toFloat(), screenHeightDp.toFloat())
    }

  @Test
  fun test_limitToMaxZoomRatio() =
    with(composeTestRule) {
      initZoomCamera()

      cameraController.setZoomRatio(UNREACHABLE_MAX_ZOOM_VALUE)

      runOnIdle {
        waitUntil(
          "expected ${cameraSession.info.maxZoom} actual: $actualCameraXZoom",
          timeoutMillis = ZOOM_RATIO_TIMEOUT,
          condition = { actualCameraXZoom == cameraSession.info.maxZoom },
        )
        assertNotEquals(UNREACHABLE_MAX_ZOOM_VALUE, actualCameraXZoom)
        assertEquals(cameraSession.info.maxZoom, zoomRatio.value)
        assertEquals(cameraSession.info.maxZoom, actualCameraXZoom)
      }
    }

  @Test
  fun test_limitToMinZoomRatio() =
    with(composeTestRule) {
      initZoomCamera()

      cameraController.setZoomRatio(UNREACHABLE_MIN_ZOOM_VALUE)

      runOnIdle {
        waitUntil(
          "expected ${cameraSession.info.minZoom}, actual $actualCameraXZoom",
          timeoutMillis = ZOOM_RATIO_TIMEOUT,
          condition = { actualCameraXZoom == cameraSession.info.minZoom },
        )
        assertNotEquals(UNREACHABLE_MIN_ZOOM_VALUE, actualCameraXZoom)
        assertEquals(cameraSession.info.minZoom, actualCameraXZoom)
      }
    }

  @Test
  fun test_zoomChangeValueToMax() =
    with(composeTestRule) {
      initZoomCamera(DEFAULT_ZOOM_VALUE)

      cameraController.setZoomRatio(cameraSession.info.maxZoom)
      runOnIdle {
        assertEquals(cameraSession.info.maxZoom, zoomRatio.value)
        assertEquals(actualCameraXZoom, zoomRatio.value)
      }
    }

  @Test
  fun test_pinchToZoom() =
    with(composeTestRule) {
      initZoomCamera(DEFAULT_ZOOM_VALUE)

      composeTestRule
        .onNodeWithTag(CAMERA_ZOOM_TAG)
        .performTouchInput {
          pinch(Offset.middle, Offset.Zero, Offset.middle, Offset.end, 50L)
        }

      runOnIdle {
        if (cameraSession.info.isZoomSupported) {
          assertNotEquals(DEFAULT_ZOOM_VALUE, zoomRatio.value)
        }
        assertEquals(actualCameraXZoom, zoomRatio.value)
      }
    }

  @Test
  fun test_pinchToZoomDisable() =
    with(composeTestRule) {
      initZoomCamera(DEFAULT_ZOOM_VALUE, isPinchToZoomEnabled = false)

      composeTestRule
        .onNodeWithTag(CAMERA_ZOOM_TAG)
        .performTouchInput {
          pinch(Offset.middle, Offset.Zero, Offset.middle, Offset.end, 50L)
        }

      runOnIdle {
        assertEquals(DEFAULT_ZOOM_VALUE, zoomRatio.value)
        assertEquals(actualCameraXZoom, zoomRatio.value)
      }
    }

  private fun ComposeContentTestRule.initZoomCamera(
    initialValue: Float = DEFAULT_ZOOM_VALUE,
    isPinchToZoomEnabled: Boolean = true,
  ) = initCameraSession { state ->
    configurationScreen = LocalConfiguration.current
    zoomRatio = rememberUpdatedState(cameraSession.state.zoomRatio)

    LaunchedEffect(initialValue) {
      cameraController.setZoomRatio(initialValue)
    }

    CameraPreview(
      modifier = Modifier.testTag(CAMERA_ZOOM_TAG),
      cameraSession = state,
      isPinchToZoomEnabled = isPinchToZoomEnabled,
    )
  }

  companion object {
    private const val CAMERA_ZOOM_TAG = "camera_zoom_tag"
    private const val UNREACHABLE_MIN_ZOOM_VALUE = -1F
    private const val UNREACHABLE_MAX_ZOOM_VALUE = 9999F
    private const val DEFAULT_ZOOM_VALUE = 1F
    private const val ZOOM_RATIO_TIMEOUT = 5000L
  }
}
