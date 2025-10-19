package com.ujizin.camposer

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.rule.GrantPermissionRule
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.session.rememberCameraSession
import org.junit.Rule

internal abstract class CameraTest {

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @get:Rule(order = 0)
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
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    )

    protected lateinit var cameraSession: CameraSession
    protected val cameraController: CameraController = CameraController()

    protected fun ComposeContentTestRule.initCameraSession(
        block: @Composable (CameraSession) -> Unit
    ) {
        setContent {
            cameraSession = rememberCameraSession(cameraController)
            block(cameraSession)
        }
        waitUntil(CAMERA_TIMEOUT) { cameraSession.isStreaming }
    }

    private companion object {
        private const val CAMERA_TIMEOUT = 10_000L
    }
}
