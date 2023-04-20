package com.ujizin.camposer

import android.util.Size
import androidx.camera.camera2.internal.compat.workaround.TargetAspectRatio.RATIO_16_9
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.ujizin.camposer.state.ImageTargetSize
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class ImageCaptureTargetSizeTest : CameraTest() {

    private lateinit var imageCaptureTargetSize: MutableState<ImageTargetSize>

    @Test
    fun test_imageCaptureTargetSize() = with(composeTestRule) {
        initImageCaptureTargetSizeCamera(ImageTargetSize(RATIO_16_9))

        runOnIdle {
            assertEquals(
                cameraState.imageCaptureTargetSize,
                ImageTargetSize(RATIO_16_9)
            )
        }

        imageCaptureTargetSize.value = ImageTargetSize(Size(1600, 1200))

        runOnIdle {
            assertEquals(
                cameraState.imageCaptureTargetSize,
                ImageTargetSize(Size(1600, 1200))
            )
        }
    }

    private fun ComposeContentTestRule.initImageCaptureTargetSizeCamera(
        initialValue: ImageTargetSize
    ) = initCameraState { state ->
        imageCaptureTargetSize = remember { mutableStateOf(initialValue) }
        CameraPreview(
            cameraState = state,
            imageCaptureTargetSize = imageCaptureTargetSize.value,
        )
    }
}
