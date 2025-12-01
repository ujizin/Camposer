package com.ujizin.camposer.codescanner

/**
 * Listener interface for handling results from the code analysis process.
 *
 * This functional interface is used to receive callbacks when a barcode, QR code,
 * or other supported code format is detected and successfully scanned within the camera frame.
 *
 * @see CodeResult
 */
public fun interface CodeAnalyzerListener {
  public fun onCodeScanned(result: CodeResult)
}
