package com.ujizin.camposer.codescanner

/**
 * Represents a corner point of a detected code in DP.
 *
 * @property x The x-axis coordinate in DP.
 * @property y The y-axis coordinate in DP.
 */
public class CornerPointer(
  public val x: Int,
  public val y: Int,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is CornerPointer) return false

    if (other.x != x) return false
    return other.y == y
  }

  override fun hashCode(): Int {
    var result = x
    result = 31 * result + y
    return result
  }

  override fun toString(): String = "CornerPointer(x=$x, y=$y)"
}
