package com.ujizin.camposer.state.properties

import com.ujizin.camposer.state.properties.ScaleType.FillCenter
import com.ujizin.camposer.state.properties.ScaleType.FillEnd
import com.ujizin.camposer.state.properties.ScaleType.FillStart
import com.ujizin.camposer.state.properties.ScaleType.FitCenter
import com.ujizin.camposer.state.properties.ScaleType.FitEnd
import com.ujizin.camposer.state.properties.ScaleType.FitStart

/**
 * Represents the scale type used to determine how the camera preview should be scaled
 * to fit within its container.
 *
 * This enum defines various strategies for scaling the source content (camera preview)
 * to the destination bounds (UI component)
 *
 * @property FitStart Scales the source so that it fits entirely inside the destination,
 * aligned to the start (top/left). One axis will be exact, the other may be smaller (letterboxing).
 * On iOS, this behaves the same as [FitCenter].
 *
 * @property FitCenter Scales the source so that it fits entirely inside the destination, centered.
 * One axis will be exact, the other may be smaller (letterboxing).
 *
 * @property FitEnd Scales the source so that it fits entirely inside the destination, aligned to
 * the end (bottom/right). One axis will be exact, the other may be smaller (letterboxing).
 * On iOS, this behaves the same as [FitCenter].
 *
 * @property FillStart Scales the source so that it fills the entire destination, aligned to the
 * start (top/left). The content may be cropped if aspect ratios differ.
 * On iOS, this behaves the same as [FillCenter].
 *
 * @property FillCenter Scales the source so that it fills the entire destination, centered.
 * The content may be cropped if aspect ratios differ.
 *
 * @property FillEnd Scales the source so that it fills the entire destination, aligned to the
 * end (bottom/right). The content may be cropped if aspect ratios differ.
 * On iOS, this behaves the same as [FillCenter].
 */
public expect enum class ScaleType {
  FitStart,
  FitCenter,
  FitEnd,
  FillStart,
  FillCenter,
  FillEnd,
}
