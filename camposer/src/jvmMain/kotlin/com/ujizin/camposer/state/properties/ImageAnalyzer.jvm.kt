package com.ujizin.camposer.state.properties

import androidx.compose.runtime.Stable

/**
 * Desktop image analyzer.
 *
 * Receives one [CameraFrame] at a time on a background thread. Frames that arrive while the
 * analyzer is busy are dropped, so a slow analyzer never builds up a backlog.
 *
 * @param analyzer callback invoked with each analyzed frame.
 * @see com.ujizin.camposer.session.rememberImageAnalyzer
 */
@Stable
public actual class ImageAnalyzer(
  internal val analyzer: Analyzer,
) {
  public fun interface Analyzer {
    public fun analyze(frame: CameraFrame)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ImageAnalyzer) return false

    return analyzer == other.analyzer
  }

  override fun hashCode(): Int = analyzer.hashCode()
}
