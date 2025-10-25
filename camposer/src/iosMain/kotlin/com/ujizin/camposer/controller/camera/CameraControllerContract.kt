import com.ujizin.camposer.command.TakePictureCommand
import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.FlashMode

public actual interface CameraControllerContract : RecordController, TakePictureCommand {
    public actual val state: CameraState?
    public actual val info: CameraInfo?
    public actual fun setZoomRatio(zoomRatio: Float)
    public actual fun setExposureCompensation(exposureCompensation: Float)
    public actual fun setFlashMode(flashMode: FlashMode)
    public actual fun setTorchEnabled(isTorchEnabled: Boolean)
}