package com.ujizin.camposer

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.ujizin.camposer.config.properties.ImageCaptureStrategy
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class ImageCaptureStrategyTest : CameraTest() {

    private lateinit var imageCaptureMode: MutableState<ImageCaptureStrategy>

    @Test
    fun test_imageCaptureMode() = with(composeTestRule) {
        initImageCaptureModeCamera(ImageCaptureStrategy.Balanced)

        Assert.assertEquals(cameraSession.config.imageCaptureStrategy, ImageCaptureStrategy.Balanced)
        imageCaptureMode.value = ImageCaptureStrategy.MaxQuality

        runOnIdle {
            Assert.assertEquals(
                ImageCaptureStrategy.MaxQuality,
                cameraSession.config.imageCaptureStrategy
            )
        }
    }

    private fun ComposeContentTestRule.initImageCaptureModeCamera(
        initialValue: ImageCaptureStrategy
    ) = initCameraSession { state ->
        imageCaptureMode = remember { mutableStateOf(initialValue) }
        CameraPreview(
            cameraSession = state,
            captureStrategy = imageCaptureMode.value,
        )
    }
}