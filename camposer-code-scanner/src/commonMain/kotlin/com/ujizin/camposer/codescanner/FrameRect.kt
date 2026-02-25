package com.ujizin.camposer.codescanner

/**
 * Represents a rectangle frame, used for scanning codes.
 *
 * @param left The left coordinate in DP.
 * @param top The top coordinate in DP.
 * @param right The right coordinate in DP.
 * @param bottom The bottom coordinate in DP.
 */
public class FrameRect(
  public val left: Int,
  public val top: Int,
  public val right: Int,
  public val bottom: Int,
) {
  public val width: Int
    get() = right - left

  public val height: Int
    get() = bottom - top

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is FrameRect) return false

    if (left != other.left) return false
    if (top != other.top) return false
    if (right != other.right) return false
    return bottom == other.bottom
  }

  override fun hashCode(): Int {
    var result = left
    result = 31 * result + top
    result = 31 * result + right
    result = 31 * result + bottom
    return result
  }

  override fun toString(): String = "FrameRect(left=$left, top=$top, right=$right, bottom=$bottom)"
}
