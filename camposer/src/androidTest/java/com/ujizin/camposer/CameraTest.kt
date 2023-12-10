package com.ujizin.camposer

import android.Manifest
import android.os.Build
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
        *mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
                add(Manifest.permission.READ_MEDIA_AUDIO)
                add(Manifest.permission.READ_MEDIA_VIDEO)
                add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
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
