import com.ujizin.camposer.command.AndroidTakePictureCommand
import com.ujizin.camposer.controller.record.AndroidRecordController

public actual interface CameraControllerContract : AndroidRecordController,
    AndroidTakePictureCommand