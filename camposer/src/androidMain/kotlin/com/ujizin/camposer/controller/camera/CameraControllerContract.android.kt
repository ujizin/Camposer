import com.ujizin.camposer.command.AndroidTakePictureCommand
import com.ujizin.camposer.controller.record.AndroidRecordController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode
import kotlinx.coroutines.flow.StateFlow

public actual interface CameraControllerContract : AndroidRecordController,
    AndroidTakePictureCommand {
    public actual val state: CameraState?
    public actual val info: CameraInfo?
    public actual val isRunning: StateFlow<Boolean>
    public actual fun setZoomRatio(zoomRatio: Float)
    public actual fun setExposureCompensation(exposureCompensation: Float)
    public actual fun setFlashMode(flashMode: FlashMode)
    public actual fun setTorchEnabled(isTorchEnabled: Boolean)
}