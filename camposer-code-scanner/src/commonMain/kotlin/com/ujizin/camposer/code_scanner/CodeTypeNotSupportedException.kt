package com.ujizin.camposer.code_scanner

public class CodeTypeNotSupportedException(
    public val codeType: CodeType,
    message: String = "Code type $codeType is not supported by this device.",
) : Exception(message)