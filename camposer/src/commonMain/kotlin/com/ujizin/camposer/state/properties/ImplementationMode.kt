package com.ujizin.camposer.state.properties

/**
 * The implementation mode of the Camera Preview. (Android Only!)
 *
 * @property Compatible The [Compatible] mode uses a `TextureView` for the preview.
 * This mode supports complex UI hierarchies and transformations better but consumes more battery
 * and resources compared to [Performance].
 *
 * @property Performance The [Performance] mode uses a `SurfaceView` for the preview.
 * While this is generally more efficient and provides better battery life, it may have compatibility issues
 * with certain UI hierarchies or transformations (like transparency or rotation).
 */
public enum class ImplementationMode {
  Compatible,
  Performance,
  ;

  public val inverse: ImplementationMode
    get() = when (this) {
      Compatible -> Performance
      else -> Compatible
    }
}
