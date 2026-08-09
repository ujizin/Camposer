package com.ujizin.camposer.annotation

/**
 * Marks declarations that are Camposer implementation details.
 *
 * These declarations are public only because Kotlin requires a public supertype for a public class.
 * They are not part of the supported API surface, and they may change or be removed in any release.
 *
 * Use [com.ujizin.camposer.controller.camera.CameraController] instead.
 */
@RequiresOptIn(
  level = RequiresOptIn.Level.ERROR,
  message =
    "This is a Camposer implementation detail. Use CameraController instead. Opt in with " +
      "@OptIn(InternalCamposerApi::class) only if you understand it may change or be removed.",
)
@Retention(AnnotationRetention.BINARY)
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY,
)
public annotation class InternalCamposerApi
