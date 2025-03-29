package com.ujizin.sample.router

import kotlinx.serialization.Serializable

@Serializable
sealed interface Router {
    @Serializable
    data object Camera : Router

    @Serializable
    data object Gallery : Router

    @Serializable
    data object Configuration : Router

    @Serializable
    data class Preview(val path: String) : Router
}
