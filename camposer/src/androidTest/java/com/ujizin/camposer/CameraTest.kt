package com.ujizin.camposer

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.rule.GrantPermissionRule
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.rememberCameraState
import org.junit.Rule

internal abstract class CameraTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    protected lateinit var cameraState: CameraState

    protected fun ComposeContentTestRule.initCameraState(
        block: @Composable (CameraState) -> Unit
    ) {
        setContent {
            cameraState = rememberCameraState()
            block(cameraState)
        }
        waitUntil(CAMERA_TIMEOUT) { cameraState.isStreaming }
    }

    private companion object {
        private const val CAMERA_TIMEOUT = 2_500L
    }
}
