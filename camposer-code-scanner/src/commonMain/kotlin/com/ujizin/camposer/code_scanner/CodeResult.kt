package com.ujizin.camposer.code_scanner

/**
 * Result from code scanner.
 *
 * @param type type of code (e.g. QR Code, Aztec, etc).
 * @param text the value from the code scanned.
 * @param frameRect the rectangle frame from the code scanned.
 * @param corners the list of corner points from the code scanned.
 */
public class CodeResult internal constructor(
    public val type: CodeType,
    public val text: String,
    public val frameRect: FrameRect,
    public val corners: List<CornerPointer>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CodeResult) return false

        if (type != other.type) return false
        if (text != other.text) return false
        if (frameRect != other.frameRect) return false
        return corners == other.corners
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + frameRect.hashCode()
        result = 31 * result + corners.hashCode()
        return result
    }

    override fun toString(): String {
        return "CodeResult(type=$type, text=$text, frameRect=$frameRect, cornerPoints=$corners)"
    }
}