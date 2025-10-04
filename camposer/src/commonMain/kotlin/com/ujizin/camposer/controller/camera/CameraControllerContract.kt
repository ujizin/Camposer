import com.ujizin.camposer.command.TakePictureCommand
import com.ujizin.camposer.controller.record.RecordController

public expect interface CameraControllerContract : RecordController, TakePictureCommand
