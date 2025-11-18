package com.ujizin.camposer.code_scanner.model

/**
 * Code Result from scanner.
 * */
public data class CodeResult(
    public val type: CodeType,
    public val text: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CodeResult) return false

        if (type != other.type) return false
        return text == other.text
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }
}