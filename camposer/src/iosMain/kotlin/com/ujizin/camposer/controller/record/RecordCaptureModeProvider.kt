package com.ujizin.camposer.controller.record

import com.ujizin.camposer.config.properties.CaptureMode

internal fun interface RecordCaptureModeProvider {
    fun get(): CaptureMode
}
