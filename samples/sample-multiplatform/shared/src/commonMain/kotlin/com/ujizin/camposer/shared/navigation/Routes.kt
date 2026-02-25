package com.ujizin.camposer.shared.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed interface Routes : NavKey {

  @Serializable
  data object CameraRoute : Routes

  @Serializable
  data object PermissionRoute : Routes

  companion object {
    internal val config: SavedStateConfiguration = SavedStateConfiguration {
      serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
          subclass(CameraRoute::class, CameraRoute.serializer())
          subclass(PermissionRoute::class, PermissionRoute.serializer())
        }
      }
    }
  }
}