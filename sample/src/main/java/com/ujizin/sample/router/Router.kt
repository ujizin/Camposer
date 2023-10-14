package com.ujizin.sample.router

import android.net.Uri

sealed class Router(val route: String) {
    object Camera : Router("camera")
    object Gallery : Router("gallery")
    object Configuration : Router("configuration")
    object Preview : Router("preview/{${Args.Path}}") {
        fun createRoute(path: String) = "preview/${Uri.encode(path)}"
    }
}
