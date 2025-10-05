package com.ujizin.camposer.state

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.camera.core.MeteringPointFactory
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.command.AndroidTakePictureCommand
import com.ujizin.camposer.command.DefaultTakePictureCommand
import com.ujizin.camposer.config.CameraConfig
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.controller.record.AndroidRecordController
import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.extensions.compatMainExecutor
import com.ujizin.camposer.info.AndroidCameraInfo
import com.ujizin.camposer.info.CameraInfo
import java.util.concurrent.Executor

/**
 * A state object that can be hoisted to control camera, take picture or record video.
 *
 * To be created use [rememberCameraState].
 * */
@Stable
public actual class CameraState private constructor(
    context: Context,
    public val controller: LifecycleCameraController,
    private val cameraController: CameraController,
    private val mainExecutor: Executor = context.compatMainExecutor,
    private val androidRecordController: AndroidRecordController,
    private val androidTakePictureCommand: AndroidTakePictureCommand,
    public actual val info: CameraInfo,
    public actual val config: CameraConfig,
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
            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager,
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
        controller = controller,
        cameraController = cameraController,
        mainExecutor = mainExecutor,
        androidRecordController = androidRecordController,
        androidTakePictureCommand = androidTakePictureCommand,
        info = info,
        config = CameraConfig(
            mainExecutor = mainExecutor,
            controller = controller,
            cameraInfo = info,
        )
    )

    internal var meteringPointFactory: MeteringPointFactory? = null


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
        controller.initializationFuture.addListener({
            cameraController.initialize(
                recordController = androidRecordController,
                takePictureCommand = androidTakePictureCommand,
            )
            config.rebindCamera = ::rebindCamera
            rebindCamera()
            isInitialized = true
        }, mainExecutor)
    }

    @SuppressLint("RestrictedApi")
    private fun rebindCamera() {
        // Disable from pinch to zoom from cameraX controller
        controller.isPinchToZoomEnabled = false
        info.bind(
            lensFacing = config.camSelector.selector.lensFacing,
            meteringPoint = meteringPointFactory?.createPoint(0F, 0F),
        )
    }

    internal fun dispose() {
        controller.unbind()
        meteringPointFactory = null
    }
}
