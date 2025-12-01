package com.ujizin.camposer.state.properties

import com.ujizin.camposer.state.properties.FlashMode.Auto
import com.ujizin.camposer.state.properties.FlashMode.Off
import com.ujizin.camposer.state.properties.FlashMode.On

/**
 * Flash modes for Camera.
 *
 * @param On Flash is always on.
 * @param Auto Flash is automatic (only when needed).
 * @param Off Flash is always off.
 *
 * @see com.ujizin.camposer.controller.camera.CameraController.setFlashMode
 * @see com.ujizin.camposer.controller.camera.CameraController.setTorchEnabled
 */
public expect enum class FlashMode {
  On,
  Auto,
  Off,
}
