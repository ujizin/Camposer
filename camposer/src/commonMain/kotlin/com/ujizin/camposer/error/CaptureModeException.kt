package com.ujizin.camposer.error

import com.ujizin.camposer.state.properties.CaptureMode

public class CaptureModeException(
  expectedCaptureMode: CaptureMode,
  message: String = "Capture mode must be $expectedCaptureMode",
) : Exception(message)
