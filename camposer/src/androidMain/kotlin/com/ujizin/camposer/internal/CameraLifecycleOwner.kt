package com.ujizin.camposer.internal

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * A per-session [LifecycleOwner] that mirrors a parent lifecycle but can be
 * independently destroyed.
 *
 * CameraX's [androidx.camera.view.LifecycleCameraController.unbind] internally
 * calls [androidx.camera.core.CameraX]'s `ProcessCameraProvider.unbindAll()`,
 * which is a **process-wide** operation that tears down every camera session.
 * By binding each camera controller to its own [CameraLifecycleOwner] instead
 * of the Activity/Fragment lifecycle, disposing one session only destroys its
 * child lifecycle — and `ProcessCameraProvider` unbinds only the use cases
 * that were bound to that specific owner, leaving other sessions intact.
 */
internal class CameraLifecycleOwner(
  private val parent: LifecycleOwner,
) : LifecycleOwner {
  private val registry = LifecycleRegistry(this)

  private val parentObserver = LifecycleEventObserver { _, event ->
    if (registry.currentState != Lifecycle.State.DESTROYED) {
      registry.handleLifecycleEvent(event)
    }
  }

  init {
    val parentState = parent.lifecycle.currentState
    if (parentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
      registry.currentState = parentState
      parent.lifecycle.addObserver(parentObserver)
    }
  }

  override val lifecycle: Lifecycle get() = registry

  fun dispose() {
    parent.lifecycle.removeObserver(parentObserver)
    if (registry.currentState != Lifecycle.State.DESTROYED) {
      registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
  }
}
