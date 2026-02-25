package com.ujizin.camposer.state.properties

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
public enum class FlashMode {
  On,
  Auto,
  Off,
  ;

  public val inverse: FlashMode
    get() = when (this) {
      On -> Off
      else -> On
    }
}
