package com.ujizin.camposer.controller.record

import com.ujizin.camposer.state.CaptureMode

internal fun interface RecordCaptureModeProvider {
    fun get(): CaptureMode
}
