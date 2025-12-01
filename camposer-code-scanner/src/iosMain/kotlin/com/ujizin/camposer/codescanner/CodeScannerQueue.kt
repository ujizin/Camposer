package com.ujizin.camposer.codescanner

import platform.darwin.dispatch_queue_create
import platform.darwin.dispatch_queue_t

internal object CodeScannerQueue {
  internal val codeAnalyzerQueue: dispatch_queue_t =
    dispatch_queue_create(
      label = "Camposer/code_analyzer_queue",
      attr = null,
    )
}
