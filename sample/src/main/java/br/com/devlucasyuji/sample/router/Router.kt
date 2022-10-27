package br.com.devlucasyuji.sample.router

sealed class Router(val route: String) {
    object Camera : Router("camera")
    object Gallery : Router("gallery")
    object Configuration : Router("configuration")
}
