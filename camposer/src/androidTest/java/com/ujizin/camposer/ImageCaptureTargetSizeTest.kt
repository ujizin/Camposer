package com.ujizin.camposer

import android.util.Size
import androidx.camera.camera2.internal.compat.workaround.TargetAspectRatio.RATIO_16_9
import androidx.camera.view.CameraController.OutputSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class ImageCaptureTargetSizeTest : CameraTest() {

    private lateinit var imageCaptureTargetSize: MutableState<OutputSize>

    @Test
    fun test_imageCaptureTargetSize() = with(composeTestRule) {
        initImageCaptureTargetSizeCamera(OutputSize(RATIO_16_9))

        runOnIdle { assertEqualSize(cameraState.imageCaptureTargetSize, OutputSize(RATIO_16_9)) }

        imageCaptureTargetSize.value = OutputSize(Size(1600, 1200))

        runOnIdle { assertEqualSize(cameraState.imageCaptureTargetSize, OutputSize(Size(1600, 1200))) }
    }

    private fun ComposeContentTestRule.initImageCaptureTargetSizeCamera(
        initialValue: OutputSize
    ) = initCameraState { state ->
        imageCaptureTargetSize = remember { mutableStateOf(initialValue) }
        CameraPreview(
            cameraState = state,
            imageCaptureTargetSize = imageCaptureTargetSize.value,
        )
    }

    private fun assertEqualSize(
        size1: OutputSize?,
        size2: OutputSize?
    ) {
        assertEquals(size1?.resolution?.height, size2?.resolution?.height)
        assertEquals(size1?.resolution?.width, size2?.resolution?.width)
        assertEquals(size1?.aspectRatio, size2?.aspectRatio)
    }
}
