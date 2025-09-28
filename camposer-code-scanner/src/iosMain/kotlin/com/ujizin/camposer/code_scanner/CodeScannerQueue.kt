package com.ujizin.camposer.code_scanner

import platform.darwin.dispatch_queue_create
import platform.darwin.dispatch_queue_t

public object CodeScannerQueue {

    public val codeAnalyzerQueue: dispatch_queue_t = dispatch_queue_create(
        label = "Camposer/code_analyzer_queue",
        attr = null,
    )
}