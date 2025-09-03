package com.ujizin.camposer

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.ujizin.camposer.state.ImageCaptureMode
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class ImageCaptureModeTest : CameraTest() {

    private lateinit var imageCaptureMode: MutableState<ImageCaptureMode>

    @Test
    fun test_imageCaptureMode() = with(composeTestRule) {
        initImageCaptureModeCamera(ImageCaptureMode.MinLatency)

        Assert.assertEquals(cameraState.imageCaptureMode, ImageCaptureMode.MinLatency)
        imageCaptureMode.value = ImageCaptureMode.MaxQuality

        runOnIdle {
            Assert.assertEquals(
                cameraState.imageCaptureMode,
                ImageCaptureMode.MaxQuality
            )
        }
    }

    private fun ComposeContentTestRule.initImageCaptureModeCamera(
        initialValue: ImageCaptureMode
    ) = initCameraState { state ->
        imageCaptureMode = remember { mutableStateOf(initialValue) }
        CameraPreview(
            cameraState = state,
            imageCaptureMode = imageCaptureMode.value,
        )
    }
}