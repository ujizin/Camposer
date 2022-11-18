package com.ujizin.camposer

import android.Manifest
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.rule.GrantPermissionRule
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

}
