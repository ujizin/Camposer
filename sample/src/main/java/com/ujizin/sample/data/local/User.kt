package com.ujizin.sample.data.local

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val usePinchToZoom: Boolean,
    val useTapToFocus: Boolean,
    val useCamFront: Boolean,
) {
    companion object {
        val Default = User(usePinchToZoom = true, useTapToFocus = true, useCamFront = false)
    }
}