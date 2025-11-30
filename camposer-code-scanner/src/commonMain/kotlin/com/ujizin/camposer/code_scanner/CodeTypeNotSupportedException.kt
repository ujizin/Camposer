package com.ujizin.camposer.code_scanner

/**
 * Exception thrown when a specific [CodeType] is not supported by the device's camera scanner.
 *
 * This exception indicates that the requested barcode type format cannot be detected
 * or processed by the underlying hardware or scanning library on the current device.
 *
 * @property codeType The specific code type format that caused the exception.
 * @param message The detail message for this exception. Defaults to a standard error message including the code type.
 */
public class CodeTypeNotSupportedException(
    public val codeType: CodeType,
    message: String = "Code type $codeType is not supported by this device.",
) : Exception(message)
