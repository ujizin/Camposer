package com.ujizin.camposer

import android.Manifest
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule

internal abstract class CameraTest {

    @get:Rule
    val cameraAccess: GrantPermissionRule
        get() = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @get:Rule
    val composeTestRule
        get() = createComposeRule()

}
