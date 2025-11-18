package com.ujizin.camposer.session

import android.content.Context
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LifecycleOwner
import com.ujizin.camposer.command.AndroidTakePictureCommand
import com.ujizin.camposer.command.DefaultTakePictureCommand
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.controller.record.AndroidRecordController
import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.extensions.compatMainExecutor
import com.ujizin.camposer.info.AndroidCameraInfo
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.CameraState
import java.util.concurrent.Executor

/**
 * A state object that can be hoisted to control camera, take picture or record video.
 *
 * To be created use [rememberCameraSession].
 * */
@Stable
public actual class CameraSession private constructor(
    context: Context,
    public val cameraXController: LifecycleCameraController,
    public val controller: CameraController,
    private val mainExecutor: Executor = context.compatMainExecutor,
    private val androidRecordController: AndroidRecordController,
    private val androidTakePictureCommand: AndroidTakePictureCommand,
    public actual val info: CameraInfo,
    public actual val state: CameraState,
) {

    public constructor(context: Context, cameraController: CameraController) : this(
        context = context,
        cameraController = cameraController,
        controller = LifecycleCameraController(context),
    )

    internal constructor(
        context: Context,
        cameraController: CameraController,
        controller: LifecycleCameraController,
    ) : this(
        context = context,
        controller = controller,
        cameraController = cameraController,
        mainExecutor = context.compatMainExecutor,
        androidRecordController = DefaultRecordController(
            cameraController = controller,
            mainExecutor = context.compatMainExecutor,
        ),
        androidTakePictureCommand = DefaultTakePictureCommand(
            controller = controller,
            mainExecutor = context.compatMainExecutor,
            contentResolver = context.contentResolver,
        ),
        info = CameraInfo(
            cameraInfo = AndroidCameraInfo(controller),
        ),
    )

    internal constructor(
        context: Context,
        controller: LifecycleCameraController,
        cameraController: CameraController,
        mainExecutor: Executor,
        androidRecordController: AndroidRecordController,
        androidTakePictureCommand: AndroidTakePictureCommand,
        info: CameraInfo,
    ) : this(
        context = context,
        cameraXController = controller,
        controller = cameraController,
        mainExecutor = mainExecutor,
        androidRecordController = androidRecordController,
        androidTakePictureCommand = androidTakePictureCommand,
        info = info,
        state = CameraState(
            context = context,
            mainExecutor = mainExecutor,
            controller = controller,
            cameraInfo = info,
        )
    )

    /**
     * Check if camera is streaming or not.
     * */
    public actual var isStreaming: Boolean by mutableStateOf(false)
        internal set

    /**
     * Check if camera state is initialized or not.
     * */
    public actual var isInitialized: Boolean by mutableStateOf(false)
        internal set

    init {
        controller.initialize(
            recordController = androidRecordController,
            takePictureCommand = androidTakePictureCommand,
            cameraState = state,
            cameraInfo = info,
        )
        cameraXController.initializationFuture.addListener({
            cameraXController.isPinchToZoomEnabled = false
            info.rebind()
            isInitialized = true
            controller.onSessionStarted()
        }, mainExecutor)
    }

    /**
     * This is unusual to make in camerax controller, however to update the preview view or implementation mode, this needs to be made
     * */
    internal fun rebind(lifecycle: LifecycleOwner) {
        cameraXController.unbind()
        cameraXController.bindToLifecycle(lifecycle)
    }

    internal fun dispose() {
        cameraXController.unbind()
    }
}
