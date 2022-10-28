package br.com.devlucasyuji.sample.router

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

fun NavGraphBuilder.route(router: Router, content: @Composable (NavBackStackEntry) -> Unit) {
    composable(router.route, content = content)
}

fun NavHostController.navigate(route: Router) {
    navigate(route.route)
}